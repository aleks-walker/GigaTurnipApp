package kg.kloop.android.gigaturnip.ui.tasks

import android.content.Context
import android.net.Uri
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
import kg.kloop.android.gigaturnip.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    @ApplicationContext val context: Context,
) : ViewModel() {

    fun getTask(token: String, id: Int): LiveData<Task> = liveData {
        repository.getTask(token, id).data?.let { emit(it) }
    }

    private val _formData = MutableLiveData<String>()
    val formData: LiveData<String> = _formData

    fun postFormData(value: String) {
        _formData.postValue(value)
    }

    fun updateTask(token: String, id: Int, responses: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val response = repository.updateTask(token, id, responses)
            }
        }
    }

    private val _listenersReady = MutableLiveData<Boolean>()
    val listenersReady: LiveData<Boolean> = _listenersReady

    fun setListenersReady(value: Boolean) {
        _listenersReady.postValue(value)
    }

    private val _pickFileKey = MutableLiveData<String>()
    val pickFileKey: LiveData<String> = _pickFileKey

    private val _fileUploadInfo = MutableLiveData<String>()
    val fileUploadInfo: LiveData<String> = _fileUploadInfo

    fun setPickFileKey(value: String) {
        _pickFileKey.postValue(value)
    }


    private val _compressedFilePath = MutableLiveData<String>()
    val compressedFilePath: LiveData<String> = _compressedFilePath

    private val _compressionProgress = MutableLiveData<Int>()
    val compressionProgress: LiveData<Int> = _compressionProgress

    fun uploadFiles(path: Path, uris: List<Uri>) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val jsonArray = getJsonArray(uris)
        uris.forEachIndexed { i, uri ->
            val fileName = getFileName(uri)
            val fileRef = getFileRef(storageRef, path, fileName)
            val uploadTask = fileRef.putFile(uri)

            uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
                val progress = (100.0 * bytesTransferred) / totalByteCount
                updateFileInfo(i, progress, fileName, jsonArray)
            }.addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener {
                    updateFileInfo(
                        i,
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
        val fileInfo = JsonObject().apply { add(_pickFileKey.value, jsonArray) }
        Timber.d("file info: $fileInfo")
        _fileUploadInfo.postValue(fileInfo.toString())
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

    fun compressInTheBackground(uri: Uri, destination: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val destPath = File(context.filesDir, "compressed_file").absolutePath
                Timber.d("Compressing in the background, destPath: $destPath")

                VideoCompressor.start(
                    context,
                    srcUri = uri,
                    srcPath = null,
                    destPath = destPath,
                    listener = object : CompressionListener {
                        override fun onCancelled() {
                        }

                        override fun onFailure(failureMessage: String) {
                            Timber.e(failureMessage)
                        }

                        override fun onProgress(percent: Float) {
                            _compressionProgress.postValue(percent.toInt())
                        }

                        override fun onStart() {
                        }

                        override fun onSuccess() {
                            _compressedFilePath.postValue(destPath)
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
    }


}