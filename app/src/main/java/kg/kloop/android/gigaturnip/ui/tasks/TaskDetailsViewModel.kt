package kg.kloop.android.gigaturnip.ui.tasks

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
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
import kg.kloop.android.gigaturnip.util.Constants.KEY_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_VIDEO_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_WEBVIEW_FILE_KEY
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
    init {
        refreshTaskDetails()
        uploadWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_UPLOAD)
        compressWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_COMPRESS)
    }

    fun setUser(user: FirebaseUser?) { _user = user }

    fun compressVideos(videoUris: List<Uri>) {
        val uploadPath = getPath(_user!!.uid, _uiState.value.task!!).getUploadPath()
        Timber.d("storage path: $uploadPath")
        videoUris.forEachIndexed { i, uri ->
            compressVideo(createInputData(i, uri.toString(), uploadPath))
        }
    }

    private fun getPath(
        userId: String,
        task: Task
    ) = Path(
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
            ExistingWorkPolicy.KEEP,
            compressRequest
        ).then(uploadRequest)
            .then(cleanupRequest)
            .enqueue()
    }

    private fun createInputData(fileKey: Int, videoUri: String, uploadPath: String): Data {
        val builder = Data.Builder()
        builder.apply {
            putInt(KEY_WEBVIEW_FILE_KEY, fileKey)
            putString(KEY_VIDEO_URI, videoUri)
            putString(KEY_UPLOAD_PATH, "test/".plus(uploadPath))
        }
        return builder.build()
    }

    fun refreshTaskDetails() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously()
                val task = repository.getTask(token!!, taskId.toInt()).data!!
                _uiState.update {
                    it.copy(
                        task = task,
                        loading = false
                    )
                }
            }
        }
    }


//    private val _isTaskCompleted = MutableLiveData<Boolean>()
//    val isTaskCompleted: LiveData<Boolean> = _isTaskCompleted

    fun completeTask(responses: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val token = getTokenSynchronously()
            val response = repository.updateTask(token!!, taskId.toInt(), responses, true)
        }
    }

    fun setListenersReady(value: Boolean) {
        _uiState.update { it.copy(listenersReady = value) }
    }

    private var _pickFileKey: String? = null

    fun setPickFileKey(value: String) {
        _pickFileKey = value
        val initialState = JsonObject().apply { add(value, JsonArray()) }
        _uiState.update {
            it.copy(fileProgressState = initialState)
        }
        Timber.d("Initial state: $initialState")
    }

//    fun uploadFiles(path: Path, uris: List<Uri>) {
//        val storageRef = Firebase.storage.reference
//        val jsonArray = buildJsonArray(uris)
//        uris.forEachIndexed { index, uri ->
//            val fileName = getFileName(uri)
//            val fileRef = getFileRef(storageRef, path, fileName)
//            val uploadTask = fileRef.putFile(uri)
//
//            uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
//                val progress = (100.0 * bytesTransferred) / totalByteCount
//                updateFileInfo(index, progress, fileName, jsonArray)
//            }.addOnSuccessListener {
//                fileRef.downloadUrl.addOnSuccessListener {
//                    updateFileInfo(
//                        index,
//                        100.0,
//                        fileName,
//                        jsonArray,
//                        Constants.STORAGE_BASE_URL.plus(it.path!!)
//                    )
//                }
//            }
//        }
//    }

    fun updateFileInfo(
        fileProgress: FileProgress
    ) {
//        val json = getFileData(fileName, progress.toString(), downloadUri)
//        jsonArray.set(i, json)
        val progressStage = _uiState.value.fileProgressState
        if (progressStage != null) {
            val jsonArray = progressStage.get(_pickFileKey)?.asJsonArray
            val currentFileId = fileProgress.id!!.toInt()
            if (jsonArray!!.size() >= currentFileId.plus(1)) {
                jsonArray.set(currentFileId, fileProgress.toJsonObject())
                val fileInfo = JsonObject().apply { add(_pickFileKey, jsonArray) }
                Timber.d("file info: $fileInfo")
                _uiState.update { uiState -> uiState.copy(fileProgressState = fileInfo) }
            } else {
                jsonArray?.add(fileProgress.toJsonObject())
                val fileInfo = JsonObject().apply { add(_pickFileKey, jsonArray) }
                _uiState.update { uiState -> uiState.copy(fileProgressState = fileInfo) }
            }

        }
    }

//    fun buildJsonArray(uris: List<Uri>): JsonArray {
//        val jsonArray = JsonArray().asJsonArray
//        uris.forEach { jsonArray.add(getFileData(getFileName(it))) }
//        return jsonArray
//    }

    private fun getFileRef(
        storageRef: StorageReference,
        path: Path,
        fileName: String
    ): StorageReference {
        val ref = storageRef.child(
            """${path.campaignId}/${path.chainId}/${path.stageId}/${path.userId}/${path.taskId}/$fileName""".trimMargin())
            Timber.d("ref path: ${ref.path}")
        return(ref)
    }

//    private fun getFileData(
//        fileName: String,
//        progress: String = "0.0",
//        storagePath: String = "",
//        downloadUri: String = ""
//    ): JsonObject =
//        JsonObject().apply {
////            addProperty("progressType", progressType)
//            addProperty("storagePath", storagePath)
//            addProperty("progress", progress)
//            addProperty("fileName", fileName)
//            addProperty("downloadUri", downloadUri)
//        }


//    fun uploadCompressedFiles(uploadPath: Path, fileUris: List<Uri>) {
//        viewModelScope.launch(Dispatchers.IO) {
//            fileUris.forEach { fileUri ->
////                compressFile(
////                    fileUri,
////                    onSuccess = { filePath -> uploadFiles(uploadPath, listOf(filePath)) })
//            }
//
//        }
//    }



}