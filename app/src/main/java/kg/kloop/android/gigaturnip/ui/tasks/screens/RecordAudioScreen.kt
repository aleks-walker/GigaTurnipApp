package kg.kloop.android.gigaturnip.ui.tasks.screens

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.accompanist.permissions.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.tasks.RecordAudioViewModel
import kg.kloop.android.gigaturnip.ui.theme.DarkBlue900
import kg.kloop.android.gigaturnip.ui.theme.DarkRed
import kg.kloop.android.gigaturnip.ui.theme.Green500
import kg.kloop.android.gigaturnip.ui.theme.LightGray500
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

@ExperimentalPermissionsApi
@Composable
fun RecordAudioScreen (
    viewModel: RecordAudioViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DetailsToolbar(title = "Record Voice", onBack = onBack)
        }
    ) { RecordScreen(viewModel) }
}

@ExperimentalPermissionsApi
@Composable
fun RecordScreen(
    viewModel: RecordAudioViewModel
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    val timeState = remember { mutableStateOf("00:00") }
    val isRecording = remember { mutableStateOf(false) }
    val isPlaying = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkBlue900),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (isRecording.value) DisplayActionText("Recording...", DarkRed)
        if (isPlaying.value) DisplayActionText("Playing...", Green500)
        DisplayTimer(timeState)
        StartRecordButton(
            onClick = {
                permissionState.launchPermissionRequest()
                if (permissionState.hasPermission) {
                    viewModel.startRecord(getTime = { timeState.value = it })
                    isRecording.value = true
                }
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StopRecordButton(
                onClick = {
                    viewModel.stopRecord()
                    isRecording.value = false
                }
            )
            PlayAudioButton(
                onClick = { viewModel.playAudio(isPlaying = { isPlaying.value = it }) }
            )
            UploadButton( onClick = { viewModel.uploadAudio() })
        }
    }
}


@Composable
private fun DisplayActionText(text: String, textColor: Color) {
    Text(
        text = text,
        style = TextStyle(color = textColor, fontSize = 24.sp),
        modifier = Modifier.padding(bottom = 70.dp)
    )
}

@Composable
private fun DisplayTimer(timeState: MutableState<String>) {
    Text(
        text = timeState.value,
        style = TextStyle(fontSize = 20.sp, color = LightGray500),
        modifier = Modifier.padding(bottom = 30.dp)
    )
}

@ExperimentalPermissionsApi
@Composable
private fun StartRecordButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(backgroundColor = DarkRed),
        modifier = Modifier
            .padding(bottom = 50.dp)
            .width(70.dp)
            .height(70.dp)
            .border(width = 6.dp, shape = RoundedCornerShape(50), color = Color.White)
    ) {}
}

@Composable
private fun StopRecordButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .background(color = LightGray500, shape = RoundedCornerShape(30))
            .width(60.dp)
            .height(60.dp)
    ) {
        Icon(
            painter = painterResource(id = kg.kloop.android.gigaturnip.R.drawable.ic_stop),
            contentDescription = "stop record",
            tint = DarkRed,
            modifier = Modifier
                .width(45.dp)
                .height(45.dp)
        )
    }
}

@Composable
fun PlayAudioButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .background(color = LightGray500, shape = RoundedCornerShape(30))
            .width(60.dp)
            .height(60.dp)
    ) {
        Icon(
            painter = painterResource(id = kg.kloop.android.gigaturnip.R.drawable.ic_play),
            contentDescription = "play audio",
            tint = Green500,
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
        )
    }
}

@Composable
private fun UploadButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .background(color = LightGray500, shape = RoundedCornerShape(30))
            .width(60.dp)
            .height(60.dp)
    ) {
        Icon(
            painter = painterResource(id = kg.kloop.android.gigaturnip.R.drawable.ic_done),
            contentDescription = "done",
            tint = Green500,
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
        )
    }
}