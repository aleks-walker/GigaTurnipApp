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
import kg.kloop.android.gigaturnip.util.Constants.KEY_PATH_TO_UPLOAD
import kg.kloop.android.gigaturnip.util.Constants.KEY_VIDEO_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_WEBVIEW_FILE_ORDER_KEY
import kg.kloop.android.gigaturnip.util.Constants.STORAGE_PRIVATE_PREFIX
import kg.kloop.android.gigaturnip.util.Constants.STORAGE_PUBLIC_PREFIX
import kg.kloop.android.gigaturnip.util.Constants.TAG_CLEANUP
import kg.kloop.android.gigaturnip.util.Constants.TAG_COMPRESS
import kg.kloop.android.gigaturnip.util.Constants.TAG_UPLOAD
import kg.kloop.android.gigaturnip.util.Constants.VIDEO_MANIPULATION_WORK_NAME
import kg.kloop.android.gigaturnip.workers.CleanupWorker
import kg.kloop.android.gigaturnip.workers.CompressVideoWorker
import kg.kloop.android.gigaturnip.workers.UploadFileWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class TaskDetailsUiState(
    val task: Task? = null,
    val previousTasks: JsonArray? = null,
    val completed: Boolean = false,
    val loading: Boolean = false,
    val fileProgressState: JsonObject? = null,
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

    var uploadWorkProgress: LiveData<List<WorkInfo>>
    var compressWorkProgress: LiveData<List<WorkInfo>>

    private val workManager = WorkManager.getInstance(application)

    private var _pickedFile: WebViewPickedFile? = null

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
        val pathToUpload = getPath(
            prefix = if (_pickedFile!!.isPrivate) STORAGE_PRIVATE_PREFIX else STORAGE_PUBLIC_PREFIX,
            userId = _user!!.uid,
            task = _uiState.value.task!!
        ).getUploadPath()
        videoUris.forEachIndexed { i, uri ->
            compressVideo(createInputData(i.toString(), uri.toString(), pathToUpload))
        }
    }

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
//        workManager.pruneWork()
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

    private fun createInputData(fileKey: String, videoUri: String, uploadPath: String): Data {
        val builder = Data.Builder()
        builder.apply {
            putString(KEY_WEBVIEW_FILE_ORDER_KEY, fileKey)
            putString(KEY_VIDEO_URI, videoUri)
            putString(KEY_PATH_TO_UPLOAD, uploadPath)
        }
        return builder.build()
    }

    fun refreshTaskDetails() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously()
                val task = repository.getTaskById(token!!, taskId.toInt()).data!!
                val previousTasks = getPreviousTasks(task, token)
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

    private suspend fun getPreviousTasks(
        task: Task,
        token: String
    ): MutableList<Task> {
        val previousTasks = mutableListOf<Task>()
        task.stage.displayedPrevStages.forEach {
            previousTasks.addAll(
                repository.getTasks(
                    token,
                    caseId = task.caseId!!,
                    stageId = it
                ).data!!
            )
        }
        return previousTasks
    }


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

    fun changeTask(responses: String) {
        Timber.d(
            "prev value: ${_uiState.value.task?.responses?.toString()}\nchanged value: $responses".trimMargin()
        )
        if (_uiState.value.task?.responses?.toString() != responses) {
            viewModelScope.launch(Dispatchers.Default) {
                val token = getTokenSynchronously()
                val response = repository.updateTask(token!!, taskId.toInt(), responses, false)
            }
        }
    }

    fun setListenersReady(value: Boolean) {
        _uiState.update { it.copy(listenersReady = value) }
    }

    fun updateFileInfo(fileProgress: FileProgress) {
        val progressState = _uiState.value.fileProgressState
        if (progressState == null) {
            updateUi(JsonObject().apply { add(_pickedFile?.key, JsonObject()) })
        } else {
            val progressData = appendToProgressData(progressState, fileProgress)
            val filesProgressInfo = setProgressData(progressState, progressData)
            updateUi(filesProgressInfo)
        }
    }

    private fun appendToProgressData(
        progressState: JsonObject,
        fileProgress: FileProgress
    ): JsonObject? {
        val progressData = progressState.getAsJsonObject(_pickedFile?.key)
        progressData.apply {
            add(fileProgress.fileName, fileProgress.toJsonObject())
        }
        return progressData
    }

    private fun updateUi(fileInfo: JsonObject) {
        _uiState.update { uiState -> uiState.copy(fileProgressState = fileInfo) }
    }

    private fun setProgressData(
        progressState: JsonObject,
        progressData: JsonObject?
    ): JsonObject {
        val filesProgressInfo = progressState.apply {
            add(_pickedFile?.key, progressData)
        }
        return filesProgressInfo
    }
}