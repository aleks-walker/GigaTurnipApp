package kg.kloop.android.gigaturnip.ui.tasks.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import com.abedelazizshe.lightcompressorlibrary.Compressor
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.JsonObject
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.tasks.TaskDetailsUiState
import kg.kloop.android.gigaturnip.ui.tasks.TaskDetailsViewModel
import kg.kloop.android.gigaturnip.ui.tasks.WebAppInterface
import kg.kloop.android.gigaturnip.ui.tasks.WebPageScreen
import kg.kloop.android.gigaturnip.util.Constants
import kg.kloop.android.gigaturnip.util.Constants.KEY_DOWNLOAD_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILENAME
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_WEBVIEW_FILE_KEY
import kg.kloop.android.gigaturnip.util.Constants.PROGRESS
import timber.log.Timber

@Composable
fun TaskDetails(
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel,
) {

    val uiState by viewModel.uiState.collectAsState()
    val user = mainActivityViewModel.user.observeAsState()
    viewModel.setUser(user.value)

    val compressProgressInfos by viewModel.compressWorkProgress.observeAsState()
    val uploadProgressInfos by viewModel.uploadWorkProgress.observeAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { Compressor.isRunning = false }) { Text("Cancel") }
        compressProgressInfos.let { compressWorkInfo ->
            compressWorkInfo?.forEach { compressProgressInfo ->
                if (isSuccess(compressProgressInfo)) {
                    Text( text = compressProgressInfo.outputData.getString(KEY_UPLOAD_PATH).toString() )
                    Button(onClick = {
                        deleteFileFromStorage(
                            compressProgressInfo,
                            context
                        )
                    }) { Text("Delete") }
                    updateWebView(compressProgressInfo, viewModel)
                } else if (isRunning(compressProgressInfo)) {
                    updateWebView(compressProgressInfo, viewModel)
                }
            }
            uploadProgressInfos?.forEach { progressInfo ->
                if (isRunning(progressInfo) || isSuccess(progressInfo)) {
                    updateWebView(progressInfo, viewModel)
                }
            }
        }

        LoadingContent(
            empty = uiState.initialLoad,
            emptyContent = { FullScreenLoading() },
            loading = uiState.loading,
            onRefresh = { viewModel.refreshTaskDetails() }) {
            if (uiState.task != null && user.value != null) {
                val videoLauncher = getActivityLauncher { uris ->
//                viewModel.uploadCompressedFiles(getPath(user.value!!.uid, uiState.task!!), urls)
                    viewModel.compressVideos(uris)
                }
                val photoLauncher = getActivityLauncher { urls ->
//                    viewModel.uploadFiles(getPath(user.value!!.uid, uiState.task!!), urls)
                }
                TaskDetailsScreenContent(
                    uiState = uiState,
                    onPickVideos = { key ->
                        videoLauncher.launch("video/*")
                        viewModel.setPickFileKey(key)
                    },
                    onPickPhotos = { key ->
                        photoLauncher.launch("image/*")
                        viewModel.setPickFileKey(key)
                    },
                    onTaskSubmit = { responses -> viewModel.completeTask(responses = responses) },
                    onListenersReady = { viewModel.setListenersReady(true) },
                    onUpdate = { if (uiState.listenersReady) viewModel.setListenersReady(false) }
                )

            }
            if (uiState.completed) {
                showTaskCompletedToast()
            }
        }

    }

}

private fun isSuccess(progressInfo: WorkInfo) =
    progressInfo.state == WorkInfo.State.SUCCEEDED

private fun isRunning(progressInfo: WorkInfo) =
    progressInfo.state == WorkInfo.State.RUNNING

@Composable
private fun updateWebView(
    progressInfo: WorkInfo,
    viewModel: TaskDetailsViewModel,
) {
    Timber.d("progress info: $progressInfo")
    val fileProgress = FileProgress(
        id = progressInfo.progress.getInt(KEY_WEBVIEW_FILE_KEY, 0),
        progress = progressInfo.progress.getInt(PROGRESS, 0).toFloat(),
        storagePath = progressInfo.progress.getString(KEY_UPLOAD_PATH),
        fileName = progressInfo.progress.getString(KEY_FILENAME),
        downloadUrl = progressInfo.outputData.getString(KEY_DOWNLOAD_URI))
    if (fileProgress.id != null
        && !fileProgress.storagePath.isNullOrBlank()
        && !fileProgress.fileName.isNullOrBlank()
    ) {
//        val jsonArray = viewModel.buildJsonArray(listOf(fileName.toUri()))
//        viewModel.updateFileInfo(fileKey, progress.toDouble(), fileName, jsonArray, downloadUrl)
        Timber.d("file progress: $fileProgress")
        viewModel.updateFileInfo(fileProgress)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = fileProgress.progress.toInt().toString())
        LinearProgressIndicator(progress = fileProgress.progress/100)
    }
}

private fun deleteFileFromStorage(info: WorkInfo, context: Context) {
    val ref = FirebaseStorage.getInstance().reference
    val filePath = info.outputData.getString(KEY_FILE_PATH)!!
    ref.child(filePath).delete().addOnCompleteListener {
        Toast.makeText(
            context,
            "Deleted:$filePath",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun TaskDetailsScreenContent(
    uiState: TaskDetailsUiState,
    onTaskSubmit: (String) -> Unit,
    onPickVideos: (String) -> Unit,
    onPickPhotos: (String) -> Unit,
    onListenersReady: () -> Unit,
    onUpdate: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TaskStageDetails(uiState)
        WebPageScreen(
            modifier = Modifier
                .wrapContentSize(),
            uiState = uiState,
            urlToRender = Constants.TURNIP_VIEW_URL,
            webAppInterface = WebAppInterface(
                onSubmit = { responses -> onTaskSubmit(responses) },
                onListenersReady = onListenersReady,
                onPickVideos = { key -> onPickVideos(key) },
                onPickPhotos = { key -> onPickPhotos(key) }
            ),
            onUpdate = onUpdate,
        )
    }
}

@Composable
private fun LoadingContent(
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    loading: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
            onRefresh = onRefresh,
            content = content,
        )
    }
}

@Composable
private fun showTaskCompletedToast() {
    Toast.makeText(
        LocalContext.current,
        stringResource(R.string.task_completed),
        Toast.LENGTH_SHORT
    ).show()
}

@Composable
private fun getActivityLauncher(
    onActivityResult: (List<Uri>) -> Unit
) = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
    onActivityResult(it)
}

@Composable
private fun TaskStageDetails(
    uiState: TaskDetailsUiState
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = uiState.task!!.stage.name,
            style = MaterialTheme.typography.h4
        )
        SelectionContainer {
            Text(
                modifier = Modifier,
                text = "id: ${uiState.task.id}",
                style = MaterialTheme.typography.subtitle2,
                color = Color.LightGray
            )
        }
        Text(
            text = uiState.task.stage.description,
            style = MaterialTheme.typography.body1
        )
    }
}

data class Path(
    val userId: String,
    val campaignId: String,
    val chainId: String,
    val stageId: String,
    val taskId: String
)

fun Path.getUploadPath() =
    "${this.campaignId}/${this.chainId}/${this.stageId}/${this.userId}/${this.taskId}/"

data class FileProgress(
    val id: Int?,
    val progress: Float = 0.0f,
    val storagePath: String?,
    val fileName: String?,
    val downloadUrl: String?
)
fun FileProgress.toJsonObject(): JsonObject = JsonObject().apply {
    addProperty("progress", progress)
    addProperty("storagePath", storagePath)
    addProperty("fileName", fileName)
    addProperty("downloadUri", downloadUrl)
}