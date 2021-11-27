package kg.kloop.android.gigaturnip.ui.tasks

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.data.responses.TaskCompleteResponse
import kg.kloop.android.gigaturnip.data.responses.TaskOpenPreviousResponse
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
import kg.kloop.android.gigaturnip.util.Constants.TASK_ID
import kg.kloop.android.gigaturnip.util.Constants.VIDEO_MANIPULATION_WORK_NAME
import kg.kloop.android.gigaturnip.workers.CleanupWorker
import kg.kloop.android.gigaturnip.workers.CompressVideoWorker
import kg.kloop.android.gigaturnip.workers.UploadFileWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

data class TaskDetailsUiState(
    val task: Task? = null,
    val openTaskId: Int? = null,
    val previousTasks: JsonArray? = null,
    val completed: Boolean = false,
    val loading: Boolean = false,
    val fileProgressState: JsonObject? = JsonObject(),
    val listenersReady: Boolean = false,
    val error: Boolean = false,
    val showErrorMessage: Boolean = false
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

    private val taskId: String = savedStateHandle.get<String>(TASK_ID)!!
    private val _uiState = MutableStateFlow(TaskDetailsUiState(loading = true))
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()
    private var updateJob: Job? = null

    var uploadWorkProgress: LiveData<List<WorkInfo>>
    var compressWorkProgress: LiveData<List<WorkInfo>>

    private val workManager = WorkManager.getInstance(application)

    private var _pickedFile: WebViewPickedFile? = null

    fun pruneWork() {
        workManager.pruneWork()
    }

    fun setPickedFile(pickedFile: WebViewPickedFile) {
        _pickedFile = pickedFile
    }

    fun setOpenTaskId(value: Int?) {
        _uiState.update { it.copy(openTaskId = value) }
    }

    init {
        workManager.pruneWork()
        refreshTaskDetails()
        uploadWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_UPLOAD)
        compressWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_COMPRESS)
    }

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
        userId = FirebaseAuth.getInstance().currentUser!!.uid,
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
        loadingState()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously { errorState() }
                token?.let { loadTask(it) }
            }
        }
    }

    private fun errorState() {
        _uiState.update { it.copy(loading = false, error = true) }
    }

    private fun loadingState() {
        _uiState.update { it.copy(loading = true, error = false) }
    }

    private suspend fun loadTask(token: String) {
        val response = repository.getTaskById(token, taskId.toInt())
        val task = response.data
        if (response.message.isNullOrEmpty()
            && task != null
        ) {
            val previousTasks = getPreviousTasks(task.id.toInt(), token)
            val previousTasksJson = getPreviousTasksJson(previousTasks)
            _uiState.update {
                it.copy(
                    task = task,
                    loading = false,
                    previousTasks = previousTasksJson
                )
            }
        } else if (!response.message.isNullOrEmpty()) {
            errorState()
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
            val token = getTokenSynchronously { showErrorState() }
            token?.let { tkn ->
                val response = repository.updateTask(tkn, taskId.toInt(), responses, true)
//                Timber.d("response: ${response.body()?.string()}")
                if (response.isSuccessful) {
                    try {
                        val parsedResponse = parseTackCompleteResponse(response)
                        parsedResponse.nextTaskId?.let { taskId ->
                            Timber.d("navigate forward: $taskId")
                            _uiState.update { it.copy(openTaskId = taskId) }
                        } ?: taskCompletedState()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorState()
                    }
                } else showErrorState()
            }
        }
    }

    private fun parseTackCompleteResponse(response: Response<ResponseBody>): TaskCompleteResponse =
        Gson().fromJson(
            response.body()?.string(),
            TaskCompleteResponse::class.java
        )

    private fun taskCompletedState() {
        _uiState.update { it.copy(completed = true) }
    }

    private fun showErrorState() {
        _uiState.update { it.copy(loading = false, showErrorMessage = true) }
    }

    fun setCompleted(value: Boolean) {
        _uiState.update { it.copy(completed = value) }
    }

    fun setErrorMessage(value: Boolean) {
        _uiState.update { it.copy(showErrorMessage = value) }
    }

    fun updateTask(task: Task, responses: String) {
        Timber.d(
            ("""old value: ${_uiState.value.task?.responses?.toString()}
                new value: $responses""").trimMargin()
        )
        if (_uiState.value.task?.responses?.toString() != responses) {
            updateJob?.cancel()
            updateJob = viewModelScope.launch(Dispatchers.Default) {
                delay(INPUT_DELAY_IN_MILL)
                val token = getTokenSynchronously()
                token?.let {
                    val response = repository.updateTask(it, taskId.toInt(), responses, false)
//                  val updatedTask = task.copy(responses = JsonParser().parse(responses).asJsonObject)
//                  if (response.isSuccessful) _uiState.update { it.copy(task = updatedTask) }
                    if (response.isSuccessful) Timber.d("task updated")
                }
            }
        }
    }

    fun openPreviousTask(task: Task) {
        loadingState()
        viewModelScope.launch(Dispatchers.Default) {
            val token = getTokenSynchronously { showErrorState() }
            token?.let {
                val response = repository.openPreviousTask(it, task.id.toInt())
                if (response.isSuccessful) {
                    try {
                        val openedTaskId = parseResponse(response).id
                        _uiState.update { it.copy(openTaskId = openedTaskId) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorState()
                    }
                } else showErrorState()
            } ?: showErrorState()
        }
    }

    private fun parseResponse(response: Response<ResponseBody>) = Gson().fromJson(
        response.body()?.string(),
        TaskOpenPreviousResponse::class.java
    )

    fun setListenersReady(value: Boolean) {
        _uiState.update { it.copy(listenersReady = value) }
    }

    fun removeFromFileProgressState(fieldId: String, fileName: String) {
        val progressState = _uiState.value.fileProgressState
        Timber.d("before remove: $progressState")
        if (progressState?.get(fieldId) != null) {
            val fieldAfterRemove = progressState.get(fieldId)?.let {
                it.asJsonObject.apply {
                    remove(fileName)
                }
            }
            val updatedProgressState = progressState.apply {
                    add(fieldId, fieldAfterRemove)
            }
            Timber.d("after remove: $updatedProgressState")
            updateUi(updatedProgressState)
        }
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