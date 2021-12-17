package kg.kloop.android.gigaturnip.ui.tasks

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val FILE_EXTENSION = ".wav"
const val TEMP_FILE_NAME = "myRecord$FILE_EXTENSION"
const val FILE_PROVIDER = ".fileprovider"

data class RecordAudioUiState(
    val storageFilePath: String? = null,
)

@HiltViewModel
class RecordAudioViewModel@Inject constructor(application: Application): ViewModel() {
    private var _pickedFile: WebViewPickedFile? = null

    private val _uiState = MutableStateFlow(RecordAudioUiState())
    val uiState: StateFlow<RecordAudioUiState> = _uiState.asStateFlow()

    val context: Context = application.applicationContext
    private val localFilePath = context.filesDir?.absolutePath + "/$TEMP_FILE_NAME"      /* /data/data/com.example.gigaturnip/files/myRecord.wav  */
    private val waveRecorder = WaveRecorder(localFilePath)
    private val mediaPlayer = MediaPlayer()
    private val uri by lazy {
        getFileUri(context)
    }

    fun setPickedFile(pickedFile: WebViewPickedFile) {
        _pickedFile = pickedFile
    }

    fun setStorageFilePath(value: String) {
        _uiState.update { it.copy(storageFilePath = value) }
    }

    fun startRecord(
        getTime: (String) -> Unit
    ) {
        waveRecorder.apply {
            startRecording()
            onTimeElapsed = {
                getTime(formatTimeUnit(it * 1000))
            }
        }
    }

    fun stopRecord() {
        waveRecorder.stopRecording()
    }

    fun playAudio(
        isPlaying: (Boolean) -> Unit
    ) {
        isPlaying(true)
        mediaPlayer.apply {
            reset()
            setDataSource(localFilePath)
            prepare()
            start()
            setOnCompletionListener {
                isPlaying(false)
            }
        }
    }

    private fun getFileUri(context: Context): Uri? {
        val path = File(context.filesDir, TEMP_FILE_NAME)
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + FILE_PROVIDER, path) //  "com.example.gigaturnip.fileprovider"
    }

    fun uploadAudio() {
        val storageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("voiceRecords/" + System.currentTimeMillis() + FILE_EXTENSION)
        val uploadTask = uri?.let { storageRef.putFile(it) }
        val referPath = storageRef.path                            /*  /voiceRecords/1639579773735.wav  */
        setStorageFilePath(referPath)

        uploadTask?.addOnSuccessListener {
            Timber.i("Upload successfully")
        }?.addOnFailureListener {
            Timber.i("Upload failure")
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