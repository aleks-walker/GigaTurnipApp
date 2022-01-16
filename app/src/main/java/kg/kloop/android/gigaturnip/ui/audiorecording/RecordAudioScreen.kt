package kg.kloop.android.gigaturnip.ui.audiorecording

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.theme.DarkBlue900
import kg.kloop.android.gigaturnip.ui.theme.DarkRed
import kg.kloop.android.gigaturnip.ui.theme.Green500
import kg.kloop.android.gigaturnip.ui.theme.LightGray500

@ExperimentalPermissionsApi
@Composable
fun RecordAudioScreen (
    viewModel: RecordAudioViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    Scaffold (
        topBar = {
            DetailsToolbar(
                title = stringResource(id = R.string.audio_recording),
                onBack =  onBack
            )
        }
    ) {
        RecordAudio(viewModel, onBack)
    }
}

@ExperimentalPermissionsApi
@Composable
fun RecordAudio(
    viewModel: RecordAudioViewModel,
    onBack: () -> Unit
) {

    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isUploaded) closeRecording(uiState) { onBack() }
    var enabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkBlue900),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            if (uiState.isRecording) {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_record),
                        contentDescription = stringResource(id = R.string.recording_icon),
                        tint = DarkRed,
                        modifier = Modifier
                            .width(15.dp)
                            .height(15.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.rec),
                        style = MaterialTheme.typography.subtitle1,
                        color = LightGray500
                    )
                }
            }
            DisplayTimer(uiState.timeState)
        }
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(40.dp)
        ) {
            if (uiState.loading) CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Green500,
                strokeWidth = 4.dp)
        }

        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(top = 100.dp, bottom = 70.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.isPlaying) {
                PlayAudioButton(
                    onClick = { if (enabled) viewModel.stopAudioPlaying() else {} },
                    icon = R.drawable.ic_pause)
            } else {
                PlayAudioButton(
                    onClick = { if (enabled) viewModel.startAudioPlaying() else {} },
                    icon = R.drawable.ic_play)
            }
            if (uiState.isFileEmpty) showEmptyFileToast(LocalContext.current, viewModel)
            if (uiState.showRecordingToast) showRecordingToast(LocalContext.current, viewModel)
            if (uiState.showPlayingToast) showPlayingToast(LocalContext.current, viewModel)

            if (uiState.isRecording) {
                StopRecordButton(onClick = { viewModel.stopRecording() })
            } else {
                StartRecordButton(
                    onClick = {
                        if (enabled) {
                            permissionState.launchPermissionRequest()
                            if (permissionState.hasPermission) {
                                viewModel.startRecording()
                            }
                        } else {}
                    }
                )
            }

            UploadButton(onClick = { viewModel.uploadAudio() })
            if (uiState.showUploadingToast) {
                showUploadingToast(LocalContext.current, viewModel)
                enabled = false
            }
        }
    }
}

fun showUploadingToast(context: Context, viewModel: RecordAudioViewModel) {
    Toast.makeText(context, "Uploading", Toast.LENGTH_LONG).show()
    viewModel.setUploadingToast(false)
}

fun showPlayingToast(context: Context, viewModel: RecordAudioViewModel) {
    Toast.makeText(context, "Playing", Toast.LENGTH_LONG).show()
    viewModel.setPlayingToast(false)
}

fun showRecordingToast(context: Context, viewModel: RecordAudioViewModel) {
    Toast.makeText(context, "Recording", Toast.LENGTH_LONG).show()
    viewModel.setRecordingToast(false)
}

fun showEmptyFileToast(context: Context, viewModel: RecordAudioViewModel) {
    Toast.makeText(context, "Please record some audio", Toast.LENGTH_LONG).show()
    viewModel.setFileState(false)
}

@Composable
private fun DisplayTimer(time: String) {
    Text(
        text = time,
        style = MaterialTheme.typography.h4,
        color = LightGray500
    )
}

@Composable
private fun closeRecording(
    uiState: RecordAudioUiState,
    onBack: () -> Unit
) {
    uiState.isUploaded = false
    onBack()
}

@ExperimentalPermissionsApi
@Composable
private fun StartRecordButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .background(color = DarkRed, shape = RoundedCornerShape(50))
            .width(65.dp)
            .height(65.dp)
            .border(width = 2.dp, shape = RoundedCornerShape(50), color = Color.White)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_mic),
            contentDescription = stringResource(id = R.string.start_recording),
            tint = LightGray500,
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
        )
    }
}

@Composable
private fun StopRecordButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .background(color = LightGray500, shape = RoundedCornerShape(50))
            .width(65.dp)
            .height(65.dp)
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
fun PlayAudioButton(
    onClick: () -> Unit,
    icon: Int
) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .background(color = LightGray500, shape = RoundedCornerShape(50))
            .width(60.dp)
            .height(60.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.play),
            tint = Color.Black,
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
        )
    }
}

@Composable
private fun UploadButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(backgroundColor = LightGray500),
        modifier = Modifier
            .background(color = LightGray500, shape = RoundedCornerShape(50))
            .width(60.dp)
            .height(60.dp)
    ) {
        Text(
            text = stringResource(id = R.string.save),
            style = MaterialTheme.typography.h3,
            color = Color.Black
        )
    }
}