package kg.kloop.android.gigaturnip.ui.audiorecording

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.tasks.RecordAudioViewModel
import kg.kloop.android.gigaturnip.ui.theme.DarkBlue900
import kg.kloop.android.gigaturnip.ui.theme.DarkRed
import kg.kloop.android.gigaturnip.ui.theme.Green500
import kg.kloop.android.gigaturnip.ui.theme.LightGray500

@ExperimentalPermissionsApi
@Composable
fun RecordAudioScreen (
    viewModel: RecordAudioViewModel = hiltViewModel(),
    onBack: () -> Unit
) {/*
    Scaffold(
        topBar = {
            DetailsToolbar(
                title = stringResource(id = R.string.audio_recording),
                onBack = onBack
            )
        }
    ) { RecordScreen(viewModel) }
}

@ExperimentalPermissionsApi
@Composable
fun RecordScreen(
    viewModel: RecordAudioViewModel
) {*/
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkBlue900),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (uiState.isRecording) DisplayActionText(
            stringResource(id = R.string.recording).plus("..."),
            DarkRed
        )
        if (uiState.isPlaying) DisplayActionText(
            stringResource(id = R.string.playing).plus("..."),
            Green500
        )
        DisplayTimer(uiState.timeState)
        StartRecordButton(
            onClick = {
                permissionState.launchPermissionRequest()
                if (permissionState.hasPermission) {
                    viewModel.startRecording()
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StopRecordButton(onClick = { viewModel.stopRecording() })
            PlayAudioButton(onClick = { viewModel.playAudio() })
            UploadButton(onClick = {
                viewModel.uploadAudio()
                onBack()
            })
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
private fun DisplayTimer(time: String) {
    Text(
        text = time,
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
            painter = painterResource(id = R.drawable.ic_stop),
            contentDescription = stringResource(id = R.string.stop_recording),
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
            painter = painterResource(id = R.drawable.ic_play),
            contentDescription = stringResource(id = R.string.play),
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
            painter = painterResource(id = R.drawable.ic_done),
            contentDescription = stringResource(id = R.string.done),
            tint = Green500,
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
        )
    }
}