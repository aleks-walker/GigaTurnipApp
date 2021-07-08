package kg.kloop.android.gigaturnip.ui.tasks

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
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
                Timber.d(response.toString())
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

    fun setPickFileKey(value: String) {
        _pickFileKey.postValue(value)
    }


    private val _compressedFilePath = MutableLiveData<String>()
    val compressedFilePath: LiveData<String> = _compressedFilePath

    private val _compressionProgress = MutableLiveData<Int>()
    val compressionProgress: LiveData<Int> = _compressionProgress

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