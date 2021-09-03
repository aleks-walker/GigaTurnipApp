package kg.kloop.android.gigaturnip.ui.tasks

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kg.kloop.android.gigaturnip.ui.tasks.screens.Path
import kg.kloop.android.gigaturnip.util.Constants
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
    val listenersReady: Boolean = false
) {
    val initialLoad: Boolean
        get() = task == null && loading
}

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val taskId: String = savedStateHandle.get<String>("id")!!
    private val _uiState = MutableStateFlow(TaskDetailsUiState(loading = true))
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    init {
        refreshTaskDetails()
    }

    fun refreshTaskDetails() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = async { getTokenSynchronously() }.await()
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

    private fun compressFile(uri: Uri, onSuccess: (Uri) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoFile = File(uri.path!!)
            val videoFileName = videoFile.name
            val downloadsPath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            val desFile = File(downloadsPath, videoFileName)
            desFile.createNewFile()


            VideoCompressor.start(
                context,
                srcUri = uri,
                srcPath = null,
                destPath = desFile.path,
                listener = object : CompressionListener {
                    override fun onCancelled() {
                    }

                    override fun onFailure(failureMessage: String) {
                        Timber.e(failureMessage)
                    }

                    override fun onProgress(percent: Float) {
                        Timber.d("compression progress: ${percent.toInt()}")
                    }

                    override fun onStart() {
                        Timber.d("Compression started")
                    }

                    override fun onSuccess() {
                        Timber.d("Compression successful")
                        onSuccess(desFile.toUri())
//                        _compressedFilePath.postValue(desFile.path)
                    }


                }, configureWith = Configuration(
                    quality = VideoQuality.MEDIUM,
                    isMinBitRateEnabled = false,
                    keepOriginalResolution = false,
//                        videoHeight = 320.0 /*Double, ignore, or null*/,
//                        videoWidth = 320.0 /*Double, ignore, or null*/,
//                        videoBitrate = 3677198 /*Int, ignore, or null*/
                )
            )
        }
    }

    fun uploadCompressedFiles(uploadPath: Path, fileUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            fileUris.forEach { fileUri ->
                compressFile(
                    fileUri,
                    onSuccess = { filePath -> uploadFiles(uploadPath, listOf(filePath)) })
            }

        }
    }


}