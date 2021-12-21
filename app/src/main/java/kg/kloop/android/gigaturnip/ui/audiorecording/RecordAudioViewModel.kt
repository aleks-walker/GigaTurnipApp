package kg.kloop.android.gigaturnip.ui.tasks

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.BuildConfig
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_EXTENSION
import kg.kloop.android.gigaturnip.util.Constants.FILE_PROVIDER
import kg.kloop.android.gigaturnip.util.Constants.TEMP_AUDIO_FILE_NAME
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
    val isPlaying: Boolean = false
)

@HiltViewModel
class RecordAudioViewModel @Inject constructor(
    @Named("audioFilePath") private val filePath: String
) : ViewModel() {
    private var _pickedFile: WebViewPickedFile? = null

    private val _uiState = MutableStateFlow(RecordAudioUiState())
    val uiState: StateFlow<RecordAudioUiState> = _uiState.asStateFlow()

    private val waveRecorder = WaveRecorder(filePath)
    private val mediaPlayer = MediaPlayer()

    fun setPickedFile(pickedFile: WebViewPickedFile) {
        _pickedFile = pickedFile
    }

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

    fun playAudio() {
        setPlayingState(true)
        mediaPlayer.apply {
            reset()
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener {
                setPlayingState(false)
            }
        }
    }

    private fun setPlayingState(value: Boolean) {
        _uiState.update { it.copy(isPlaying = value) }
    }

    fun uploadAudio() {
        val storageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "voiceRecords/"
                        + System.currentTimeMillis() + AUDIO_FILE_EXTENSION
            )
        val uri: Uri = Uri.fromFile(File(filePath))
        val uploadTask = storageRef.putFile(uri)
//        storageFilePath = storageRef.path                                                             //  /voiceRecords/1639579773735.wav

        uploadTask.addOnSuccessListener {
            Timber.d("Upload successfully")
        }.addOnFailureListener { exception ->
            // TODO: handle errors
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