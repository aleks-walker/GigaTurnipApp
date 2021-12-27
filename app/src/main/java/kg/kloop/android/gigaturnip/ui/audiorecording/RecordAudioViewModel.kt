package kg.kloop.android.gigaturnip.ui.audiorecording

import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_EXTENSION
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_KEY
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_UPLOAD_PATH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


data class RecordAudioUiState(
    val timeState: String = "00:00",
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    var isUploaded: Boolean = false
)
data class UploadPath(
    val key: String,
    val path: String
)

@HiltViewModel
class RecordAudioViewModel @Inject constructor(
    @Named("audioFilePath") private val filePath: String,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

//    private var audioFileKey: String = savedStateHandle.get<String>(AUDIO_FILE_KEY)!!
    private var audioFileUploadPath: String = savedStateHandle.get<String>(AUDIO_FILE_UPLOAD_PATH)!!
//    private val uploadPath = UploadPath(audioFileKey, audioFileUploadPath)

    private val _uiState = MutableStateFlow(RecordAudioUiState())
    val uiState: StateFlow<RecordAudioUiState> = _uiState.asStateFlow()

    private val waveRecorder = WaveRecorder(filePath)
    private val mediaPlayer = MediaPlayer()

    fun startRecording() {
        setRecordingState(true)
        waveRecorder.apply {
            startRecording()
            onTimeElapsed = { updateTimeElapsed(it) }
        }
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

    fun startPlayingAudio() {
        val timer = Timer()
        setPlayingState(true)
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
            start()
        }
        updateTime(timer)
    }

    private fun playingStop(){
        setPlayingState(false)
        mediaPlayer.apply {
            reset()
            stop()
        }
    }

    private fun updateTime(timer: Timer) {
        val dur = (mediaPlayer.duration/1000).toLong()
        var i: Long = 0
        val timerTask = object: TimerTask() {
            override fun run() {
                _uiState.update { it.copy(timeState = formatTimeUnit(i * 1000) ) }
                i++
                if (i > dur) {
                    timer.cancel()
                    playingStop()
                }
            }
        }
        timer.schedule(timerTask, 1000, 1000)
    }

    fun stopPlayingAudio() {
        playingStop()
    }

    private fun setPlayingState(value: Boolean) {
        _uiState.update { it.copy(isPlaying = value) }
    }

    fun uploadAudio() {
        val storageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                audioFileUploadPath + System.currentTimeMillis() + AUDIO_FILE_EXTENSION
            )
        val storageFilePath = storageRef.path
        val uri: Uri = Uri.fromFile(File(filePath))
        val uploadTask = storageRef.putFile(uri)                                                             //  /voiceRecords/1639579773735.wav

        uploadTask.addOnSuccessListener {
            Timber.d("Upload successfully")
            _uiState.update { it.copy(isUploaded = true) }
//            return@addOnSuccessListener

        }.addOnFailureListener { exception ->
            Timber.d("Upload failure: $exception")
        }
        mediaPlayer.release()
    }

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
}