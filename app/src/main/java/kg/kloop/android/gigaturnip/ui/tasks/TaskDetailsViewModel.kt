package kg.kloop.android.gigaturnip.ui.tasks

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kg.kloop.android.gigaturnip.ui.tasks.screens.Path
import kg.kloop.android.gigaturnip.util.Constants
import kg.kloop.android.gigaturnip.util.Constants.KEY_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_VIDEO_URI
import kg.kloop.android.gigaturnip.util.Constants.TAG_CLEANUP
import kg.kloop.android.gigaturnip.util.Constants.TAG_PROGRESS
import kg.kloop.android.gigaturnip.util.Constants.TAG_UPLOAD
import kg.kloop.android.gigaturnip.util.Constants.VIDEO_MANIPULATION_WORK_NAME
import kg.kloop.android.gigaturnip.workers.CleanupWorker
import kg.kloop.android.gigaturnip.workers.CompressVideoWorker
import kg.kloop.android.gigaturnip.workers.UploadFileWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

data class TaskDetailsUiState(
    val task: Task? = null,
    val completed: Boolean = false,
    val loading: Boolean = false,
    val fileUploadInfo: String = "{}",
    val listenersReady: Boolean = false,
//    val outputWorkInfos: List<WorkInfo> = emptyList(),
//    val progressWorkInfoItems: List<WorkInfo> = emptyList()
) {
    val initialLoad: Boolean
        get() = task == null && loading
}

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
//    @ApplicationContext val context: Context,
    application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val taskId: String = savedStateHandle.get<String>("id")!!
    private val _uiState = MutableStateFlow(TaskDetailsUiState(loading = true))
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    private val workManager = WorkManager.getInstance(application)

    init {
        refreshTaskDetails()
    }

    fun compressVideo(videoUri: Uri, path: Path) {
        Timber.d("Compress video $videoUri")
//        workManager.pruneWork()
        val compressRequest = OneTimeWorkRequestBuilder<CompressVideoWorker>()
            .setInputData(
                createInputData(
                    listOf(
                        KEY_UPLOAD_PATH to getUploadPath(path),
                        KEY_VIDEO_URI to videoUri.toString()
                    )
                )
            )
            .addTag(TAG_PROGRESS)
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

    private fun createInputData(params: List<Pair<String, String>>): Data {
        val builder = Data.Builder()
        for (param in params) {
            builder.putString(param.first, param.second)
        }
        return builder.build()
    }

    private fun getUploadPath(path: Path): String {
        //TODO: remove "test"
        return """test/${path.campaignId}/${path.chainId}/${path.stageId}/${path.userId}/${path.taskId}/""".trimMargin()
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


    private val _isTaskCompleted = MutableLiveData<Boolean>()
    val isTaskCompleted: LiveData<Boolean> = _isTaskCompleted

    fun completeTask(responses: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val token = async { getTokenSynchronously() }.await()
            val response = repository.updateTask(token!!, taskId.toInt(), responses, true)
        }
    }

    fun setListenersReady(value: Boolean) {
        _uiState.update { it.copy(listenersReady = value) }
    }



    private var _pickFileKey: String? = null

    fun setPickFileKey(value: String) {
        _pickFileKey = value
    }

    fun uploadFiles(path: Path, uris: List<Uri>) {
        val storageRef = Firebase.storage.reference
        val jsonArray = getJsonArray(uris)
        uris.forEachIndexed { index, uri ->
            val fileName = getFileName(uri)
            val fileRef = getFileRef(storageRef, path, fileName)
            val uploadTask = fileRef.putFile(uri)

            uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
                val progress = (100.0 * bytesTransferred) / totalByteCount
                updateFileInfo(index, progress, fileName, jsonArray)
            }.addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener {
                    updateFileInfo(
                        index,
                        100.0,
                        fileName,
                        jsonArray,
                        Constants.STORAGE_BASE_URL.plus(it.path!!)
                    )
                }
            }
        }
    }

    private fun updateFileInfo(
        i: Int,
        progress: Double,
        fileName: String,
        jsonArray: JsonArray,
        downloadUri: String = ""
    ) {
        val json = getFileInfo(progress, fileName, downloadUri)
        jsonArray.set(i, json)
        val fileInfo = JsonObject().apply { add(_pickFileKey, jsonArray) }
        Timber.d("file info: $fileInfo")
        _uiState.update { it.copy(fileUploadInfo = fileInfo.toString()) }
    }

    private fun getJsonArray(uris: List<Uri>): JsonArray {
        val jsonArray = JsonArray().asJsonArray
        uris.forEach {
            jsonArray.add(getFileInfo(0.0, getFileName(it), ""))
        }
        return jsonArray
    }

    private fun getFileInfo(
        progress: Double,
        fileName: String,
        downloadUri: String
    ): JsonObject {
        Timber.d("Upload is $progress done")
        return getFileData(
            fileName,
            progress.toString(),
            downloadUri
        )
    }

    private fun getFileName(uri: Uri) = File(uri.path!!).name

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

    private fun getFileData(fileName: String, progress: String, downloadUri: String): JsonObject =
        JsonObject().apply {
            addProperty("progress", progress)
            addProperty("fileName", fileName)
            addProperty("downloadUri", downloadUri)
    }


    fun uploadCompressedFiles(uploadPath: Path, fileUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            fileUris.forEach { fileUri ->
//                compressFile(
//                    fileUri,
//                    onSuccess = { filePath -> uploadFiles(uploadPath, listOf(filePath)) })
            }

        }
    }


}