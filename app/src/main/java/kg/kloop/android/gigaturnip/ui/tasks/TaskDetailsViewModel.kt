package kg.kloop.android.gigaturnip.ui.tasks

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseUser
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kg.kloop.android.gigaturnip.ui.tasks.screens.FileProgress
import kg.kloop.android.gigaturnip.ui.tasks.screens.Path
import kg.kloop.android.gigaturnip.ui.tasks.screens.getUploadPath
import kg.kloop.android.gigaturnip.ui.tasks.screens.toJsonObject
import kg.kloop.android.gigaturnip.util.Constants.INPUT_DELAY_IN_MILL
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_ID
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_PATH_TO_UPLOAD
import kg.kloop.android.gigaturnip.util.Constants.STORAGE_PRIVATE_PREFIX
import kg.kloop.android.gigaturnip.util.Constants.STORAGE_PUBLIC_PREFIX
import kg.kloop.android.gigaturnip.util.Constants.TAG_CLEANUP
import kg.kloop.android.gigaturnip.util.Constants.TAG_COMPRESS
import kg.kloop.android.gigaturnip.util.Constants.TAG_UPLOAD
import kg.kloop.android.gigaturnip.util.Constants.VIDEO_MANIPULATION_WORK_NAME
import kg.kloop.android.gigaturnip.workers.CleanupWorker
import kg.kloop.android.gigaturnip.workers.CompressVideoWorker
import kg.kloop.android.gigaturnip.workers.UploadFileWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

data class TaskDetailsUiState(
    val task: Task? = null,
    val previousTasks: JsonArray? = null,
    val completed: Boolean = false,
    val loading: Boolean = false,
    val fileProgressState: JsonObject? = JsonObject(),
    val listenersReady: Boolean = false,
) {
    val initialLoad: Boolean
        get() = task == null && loading
}

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String = savedStateHandle.get<String>("id")!!
    private val _uiState = MutableStateFlow(TaskDetailsUiState(loading = true))
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()
    private var _user: FirebaseUser? = null
    private var updateJob: Job? = null

    var uploadWorkProgress: LiveData<List<WorkInfo>>
    var compressWorkProgress: LiveData<List<WorkInfo>>

    private val workManager = WorkManager.getInstance(application)

    private var _pickedFile: WebViewPickedFile? = null

    fun clearFileProgress() {
        _uiState.update {
            it.copy(fileProgressState = null)
        }
    }
    fun pruneWork() {
        workManager.pruneWork()
    }

    fun setPickedFile(pickedFile: WebViewPickedFile) {
        _pickedFile = pickedFile
    }

    init {
        workManager.pruneWork()
        refreshTaskDetails()
        uploadWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_UPLOAD)
        compressWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_COMPRESS)
    }

    fun setUser(user: FirebaseUser?) { _user = user }

    fun compressVideos(videoUris: List<Uri>) {
        videoUris.forEach { uri ->
            compressVideo(createInputData(uri.toString()))
        }
    }

    fun uploadPhotos(uris: List<Uri>) {
        uris.forEach { uri ->
            uploadPhoto(createInputData(uri.toString()))
        }
    }

    private fun makeUploadPath(): String = getPath(
        prefix = if (_pickedFile!!.isPrivate) STORAGE_PRIVATE_PREFIX else STORAGE_PUBLIC_PREFIX,
        userId = _user!!.uid,
        task = _uiState.value.task!!
    ).getUploadPath()

    private fun getPath(
        prefix: String,
        userId: String,
        task: Task
    ) = Path(
        prefix,
        userId,
        task.stage.chain.campaignId.toString(),
        task.stage.chain.id.toString(),
        task.id,
        task.stage.id,
    )

    private fun compressVideo(inputData: Data) {
        Timber.d("compress video input data: $inputData")
        val compressRequest = OneTimeWorkRequestBuilder<CompressVideoWorker>()
            .setInputData(inputData)
            .addTag(TAG_COMPRESS)
            .build()
        val uploadRequest = OneTimeWorkRequestBuilder<UploadFileWorker>()
            .addTag(TAG_UPLOAD)
            .build()
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
            .addTag(TAG_CLEANUP)
            .build()

        workManager.beginUniqueWork(
            VIDEO_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.APPEND,
            compressRequest
        ).then(uploadRequest)
            .then(cleanupRequest)
            .enqueue()
    }

    private fun uploadPhoto(inputData: Data) {
        val uploadRequest = OneTimeWorkRequestBuilder<UploadFileWorker>()
            .setInputData(inputData)
            .addTag(TAG_UPLOAD)
            .build()

        workManager.beginUniqueWork(
            VIDEO_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.APPEND,
            uploadRequest
        ).enqueue()

    }

    private fun createInputData(fileUri: String): Data {
        val builder = Data.Builder()
        builder.apply {
            putString(KEY_FILE_ID, _pickedFile!!.key)
            putString(KEY_FILE_URI, fileUri)
            putString(KEY_PATH_TO_UPLOAD, makeUploadPath())
        }
        return builder.build()
    }

    fun refreshTaskDetails() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously()!!
                val task = repository.getTaskById(token, taskId.toInt()).data!!
                val previousTasks = getPreviousTasks(task.id.toInt(), token)
                val previousTasksJson = getPreviousTasksJson(previousTasks)
                _uiState.update {
                    it.copy(
                        task = task,
                        loading = false,
                        previousTasks = previousTasksJson
                    )
                }
            }
        }
    }

    private fun getPreviousTasksJson(tasks: MutableList<Task>): JsonArray {
        val previousTasksJson = JsonArray()
        tasks.forEach { task ->
            previousTasksJson.add(
                JsonObject().apply {
                    add("jsonSchema", task.stage.jsonSchema.toJsonObject())
                    add("uiSchema", task.stage.uiSchema.toJsonObject())
                    add("responses", task.responses)
                }
            )
        }
        return previousTasksJson
    }

    private suspend fun getPreviousTasks(taskId: Int, token: String): MutableList<Task> =
        repository.getPreviousTasks(token, taskId).data.orEmpty().toMutableList()

    fun completeTask(responses: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val token = getTokenSynchronously()
            val response = repository.updateTask(token!!, taskId.toInt(), responses, true)
            _uiState.update { it.copy(completed = true) }
        }
    }

    fun setCompleted(value: Boolean) {
        _uiState.update { it.copy(completed = value) }
    }

    fun updateTask(task: Task, responses: String) {
        Timber.d(
            ("prev value: ${_uiState.value.task?.responses?.toString()}\n" +
                    "changed value: $responses").trimMargin()
        )
        if (_uiState.value.task?.responses?.toString() != responses) {
            updateJob?.cancel()
            updateJob = viewModelScope.launch(Dispatchers.Default) {
                delay(INPUT_DELAY_IN_MILL)
                val token = getTokenSynchronously()
                val response = repository.updateTask(token!!, taskId.toInt(), responses, false)
//                val updatedTask = task.copy(responses = JsonParser().parse(responses).asJsonObject)
//                if (response.isSuccessful) _uiState.update { it.copy(task = updatedTask) }
            }
        }
    }

    fun setListenersReady(value: Boolean) {
        _uiState.update { it.copy(listenersReady = value) }
    }

    fun updateFileInfo(fileProgress: FileProgress) {
        val progressState = _uiState.value.fileProgressState
        Timber.d("progress state before: $progressState")
        progressState?.let {
            if (progressState.get(fileProgress.fileId) == null) {
                updateUi(progressState.apply { add(fileProgress.fileId, JsonObject()) })
            }
            val progressData = appendToProgressData(progressState, fileProgress)
            Timber.d("progress data: $progressData")
            val filesProgressInfo = setProgressData(
                fileProgress.fileId,
                progressState,
                progressData
            )
            Timber.d("files progress info: $filesProgressInfo")
            updateUi(filesProgressInfo)
        }
        Timber.d("progress state after: ${_uiState.value.fileProgressState}")
    }

    private fun appendToProgressData(
        progressState: JsonObject,
        fileProgress: FileProgress
    ): JsonObject? {
        val progressData = progressState.getAsJsonObject(fileProgress.fileId)
        progressData.apply {
            add(fileProgress.fileName, fileProgress.toJsonObject())
        }
        return progressData
    }

    private fun updateUi(fileInfo: JsonObject) {
        _uiState.update { uiState -> uiState.copy(fileProgressState = fileInfo) }
    }

    private fun setProgressData(
        fileId: String,
        progressState: JsonObject,
        progressData: JsonObject?
    ): JsonObject {
        val filesProgressInfo = progressState.apply {
            add(fileId, progressData)
        }
        return filesProgressInfo
    }
}