package kg.kloop.android.gigaturnip.ui.audiorecording

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.util.Constants
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_KEY
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_MANIPULATION_WORK_NAME
import kg.kloop.android.gigaturnip.util.Constants.TAG_AUDIO_CLEANUP
import kg.kloop.android.gigaturnip.util.Constants.TAG_UPLOAD
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


data class RecordAudioUiState(
    val timeState: String = "00:00",
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    var isUploaded: Boolean = false,
    var fileState: Boolean = false,
    val showRecordingToast: Boolean = false,
    val showPlayingToast: Boolean = false,
    val showUploadingToast: Boolean = false,
    var loading: Boolean = false,
    val fileProgressState: JsonObject? = JsonObject(),
)

@HiltViewModel
class RecordAudioViewModel @Inject constructor(
    @Named("audioFilePath") private val filePath: String,
    savedStateHandle: SavedStateHandle,
    application: Application
) : ViewModel() {

    private var audioFileKey: String = savedStateHandle.get<String>(AUDIO_FILE_KEY)!!
    private lateinit var storagePath: String
    private var audioFileUploadPath: String = savedStateHandle.get<String>(AUDIO_FILE_UPLOAD_PATH)!!

    private val _uiState = MutableStateFlow(RecordAudioUiState())
    val uiState: StateFlow<RecordAudioUiState> = _uiState.asStateFlow()

    private val waveRecorder = WaveRecorder(filePath)
    private val mediaPlayer = MediaPlayer()
    private val file = File(filePath)

    var audioUploadWorkProgress: LiveData<List<WorkInfo>>
    private val workManager = WorkManager.getInstance(application)

    init {
        workManager.pruneWork()
        audioUploadWorkProgress = workManager.getWorkInfosByTagLiveData(TAG_UPLOAD)
    }

    fun startRecording() {
        setRecordingState(true)
        setRecordingToast(true)
        waveRecorder.apply {
            startRecording()
            onTimeElapsed = { updateTimeElapsed(it) }
        }
    }

    fun setRecordingToast(value: Boolean) {
        _uiState.update { it.copy(showRecordingToast = value) }
    }

    fun getEventData(): Pair<String, String> {
        return audioFileKey to storagePath
    }

    private fun updateTimeElapsed(timeElapsed: Long) {
        _uiState.update {
            it.copy(
                timeState =
                formatTimeUnit(timeElapsed * 1000)
            )
        }
    }

    fun stopRecording() {
        waveRecorder.stopRecording()
        setRecordingState(false)
    }

    private fun setRecordingState(value: Boolean) {
        _uiState.update { it.copy(isRecording = value) }
    }

    fun startAudioPlaying() {
        if (file.exists()) {
            setPlayingState(true)
            setPlayingToast(true)
            mediaPlayer.apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            updateTime()
        } else {
            setFileState(true)
        }
    }

    fun setPlayingToast(value: Boolean) {
        _uiState.update { it.copy(showPlayingToast = value) }
    }

    fun setFileState(value: Boolean) {
        _uiState.update { it.copy(fileState = value) }
    }

    fun stopAudioPlaying(){
        setPlayingState(false)
        mediaPlayer.apply {
            reset()
            stop()
        }
    }

    private fun updateTime() {
        //TODO: coroutines example
        viewModelScope.launch {
            val duration = mediaPlayer.duration.toLong()
            withTimeout(duration + 2000) {
                repeat((duration + 2000).div(1000).toInt() ){
                    _uiState.update { it.copy(timeState = formatTimeUnit(mediaPlayer.currentPosition.toLong()) ) }
                    delay(1000)
                }
            }
            stopAudioPlaying()
        }
    }

    private fun setPlayingState(value: Boolean) {
        _uiState.update { it.copy(isPlaying = value) }
    }

    fun setUploadingToast(value: Boolean) {
        _uiState.update { it.copy(showUploadingToast = value) }
    }

/*    fun uploadAudio() {
        val storageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                audioFileUploadPath.encodeUrl() + System.currentTimeMillis() + AUDIO_FILE_EXTENSION
            )
        storagePath = storageRef.path

        try {
            if (file.exists()) {
                _uiState.update { it.copy(loading = true) }
                setUploadingToast(true)
                val uri: Uri = Uri.fromFile(File(filePath))
                val uploadTask = storageRef.putFile(uri)

                uploadTask.addOnSuccessListener {
                    Timber.d("Upload successfully")
                    _uiState.update { it.copy(isUploaded = true) }
                    val deleted = file.delete()
                    Timber.i("Deleted ${file.name} - $deleted")

                }.addOnFailureListener { exception ->
                    Timber.d("Upload failure: $exception")
                }

                mediaPlayer.release()
                Timber.i("MediaPlayer released")
            } else {
                setFileState(true)
            }

        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }*/

    private fun formatTimeUnit(timeInMilliseconds: Long): String {
        return try {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds),
                TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds)
                )
            )
        } catch (e: Exception) {
            "00:00"
        }
    }

    fun uploadAudio() {
        if (file.exists()) {
            setUploadingToast(true)
            val uri: Uri = Uri.fromFile(File(filePath))
            upload(createData(uri.toString()))
            mediaPlayer.apply {
                reset()
                release()
            }
            _uiState.update { it.copy(isUploaded = true) }
        } else {
            setFileState(true)
        }
    }

    private fun upload(inputData: Data) {
        val uploadRequest = OneTimeWorkRequestBuilder<UploadAudioFileWorker>()
            .setInputData(inputData)
            .addTag(TAG_UPLOAD)
            .build()
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupAudioFileWorker>()
            .addTag(TAG_AUDIO_CLEANUP)
            .build()

        workManager.beginUniqueWork(
            AUDIO_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.APPEND,
            uploadRequest
        ).then(cleanupRequest)
            .enqueue()
    }

    private  fun createData(audioFileUri: String): Data {
        val builder = Data.Builder()
        builder.apply {
//            putString(KEY_AUDIO_FILE_ID, audioFileKey)
//            putString(KEY_AUDIO_FILE_URI, audioFileUri)
//            putString(KEY_PATH_TO_UPLOAD_AUDIO, audioFileUploadPath)
            putString(Constants.KEY_FILE_ID, audioFileKey)
            putString(Constants.KEY_FILE_URI, audioFileUri)
            putString(Constants.KEY_PATH_TO_UPLOAD, audioFileUploadPath)
        }
        return builder.build()
    }

}