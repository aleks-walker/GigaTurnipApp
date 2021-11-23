package kg.kloop.android.gigaturnip.ui.tasks.screens

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
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
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.abedelazizshe.lightcompressorlibrary.Compressor
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.JsonObject
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.components.TryAgainScreen
import kg.kloop.android.gigaturnip.ui.tasks.*
import kg.kloop.android.gigaturnip.util.Constants
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILENAME
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_ID
import kg.kloop.android.gigaturnip.util.Constants.KEY_STORAGE_REF_PATH
import kg.kloop.android.gigaturnip.util.Constants.PROGRESS
import timber.log.Timber

@Composable
fun TaskDetails(
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val compressProgressInfos by viewModel.compressWorkProgress.observeAsState()
    val uploadProgressInfos by viewModel.uploadWorkProgress.observeAsState()

    if (uiState.completed) closeTask(viewModel) { onBack() }
    if (uiState.task != null) {
        sendFileProgressToWebView(
            viewModel,
            compressProgressInfos,
            uploadProgressInfos
        )
    }
    ScreenContent(uiState, onBack, viewModel)
}

@Composable
private fun ScreenContent(
    uiState: TaskDetailsUiState,
    onBack: () -> Unit,
    viewModel: TaskDetailsViewModel,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            DetailsToolbar(
                title = uiState.task?.stage?.name.orEmpty(),
                onBack = onBack,
            )
        },
    ) {
        if (uiState.error) {
            TryAgainScreen { viewModel.refreshTaskDetails() }
        } else {
            LoadingContent(
                empty = uiState.initialLoad,
                emptyContent = { FullScreenLoading() },
                loading = uiState.loading,
                onRefresh = { viewModel.refreshTaskDetails() }) {
                val videoLauncher = getActivityLauncher { uris ->
                    viewModel.compressVideos(uris)
                }
                val photoLauncher = getActivityLauncher { uris ->
                    viewModel.uploadPhotos(uris)
                }
                if (uiState.task != null) {
                    TaskDetailsScreenContent(
                        uiState = uiState,
                        onPickVideos = { pickedFile ->
                            viewModel.pruneWork()
                            viewModel.setPickedFile(pickedFile)
                            videoLauncher.launch("video/*")
                        },
                        onPickPhotos = { pickedFile ->
                            viewModel.pruneWork()
                            viewModel.setPickedFile(pickedFile)
                            photoLauncher.launch("image/*")
                        },
                        onTaskSubmit = { responses -> viewModel.completeTask(responses = responses) },
                        onFormChange = { responses ->
                            viewModel.updateTask(
                                task = uiState.task,
                                responses = responses
                            )
                        },
                        onListenersReady = { viewModel.setListenersReady(true) },
                        onUpdate = { webview ->
                            Timber.d("WebView update")
                            if (uiState.listenersReady) {
                                webViewInitialLoad(uiState, webview)
                                viewModel.setListenersReady(false)
                            }
                            webViewFileProgressLoad(webview, uiState)
                        },
                        onCancelWork = {
                            Compressor.isRunning = false
                            cancelAllWork(context)
                        },
                        onFileDelete = { fieldId, fileName ->
                            viewModel.pruneWork()
                            viewModel.removeFromFileProgressState(fieldId, fileName)
                        },
                        onPreviewFile = { storagePath -> showPreview(storagePath, context) }
                    )
                }
            }
        }
    }
}

private fun webViewFileProgressLoad(
    webview: WebView,
    uiState: TaskDetailsUiState
) {
    Timber.d("FILE EVENT: ${uiState.fileProgressState}")
    evaluateJs(
        webview, uiState.fileProgressState.toString(),
        Constants.FILE_EVENT
    )
}

private fun webViewInitialLoad(
    uiState: TaskDetailsUiState,
    webview: WebView
) {
    val json = JsonObject().apply {
        add("jsonSchema", uiState.task?.stage?.jsonSchema?.toJsonObject())
        add("uiSchema", uiState.task?.stage?.uiSchema?.toJsonObject())
        addProperty("isComplete", uiState.task?.isComplete)
    }
    evaluateJs(
        webview, getRichText(uiState.task?.stage?.richText.orEmpty()),
        Constants.RICH_TEXT_EVENT
    )
    evaluateJs(
        webview, uiState.previousTasks.toString(),
        Constants.PREVIOUS_TASKS_EVENT
    )
    evaluateJs(webview, json.toString(), Constants.SCHEMA_EVENT)
    evaluateJs(
        webview, uiState.task?.responses.toString(),
        Constants.DATA_EVENT
    )
    Timber.d("data state: ${uiState.task?.responses}")
}

@Composable
private fun sendFileProgressToWebView(
    viewModel: TaskDetailsViewModel,
    compressProgressInfos: List<WorkInfo>?,
    uploadProgressInfos: List<WorkInfo>?
) {
    compressProgressInfos.let { compressWorkInfo ->
        compressWorkInfo?.forEach { compressProgressInfo ->
            updateWebView(compressProgressInfo, viewModel)
        }
        uploadProgressInfos?.forEach { uploadProgressInfo ->
            updateWebView(uploadProgressInfo, viewModel)
        }
    }
}

@Composable
private fun closeTask(
    viewModel: TaskDetailsViewModel,
    onBack: () -> Unit
) {
    showTaskCompletedToast()
    viewModel.setCompleted(false)
    onBack()
}

private fun updateWebView(progressInfo: WorkInfo, viewModel: TaskDetailsViewModel) {
    if (isRunning(progressInfo) || isSuccess(progressInfo)) {
        updateFileProgress(
            if (isRunning(progressInfo)) progressInfo.progress else progressInfo.outputData,
            progressInfo.state.isFinished,
            progressInfo.tags.last(),
            viewModel
        )
    }
}

private fun isSuccess(progressInfo: WorkInfo) =
    progressInfo.state == WorkInfo.State.SUCCEEDED

private fun isRunning(progressInfo: WorkInfo) =
    progressInfo.state == WorkInfo.State.RUNNING

private fun updateFileProgress(
    inputData: Data,
    isFinished: Boolean,
    workTag: String,
    viewModel: TaskDetailsViewModel,
) {
    val fileProgress = FileProgress(
        fileId = inputData.getString(KEY_FILE_ID).orEmpty(),
        fileName = inputData.getString(KEY_FILENAME),
        storagePath = inputData.getString(KEY_STORAGE_REF_PATH),
        progress = inputData.getInt(PROGRESS, 0).toFloat(),
        workTag = workTag,
        isFinished = isFinished)
    if (fileProgress.fileId.isNotBlank()
        && !fileProgress.fileName.isNullOrBlank()
    ) {
        Timber.d("file progress: $fileProgress")
        viewModel.updateFileInfo(fileProgress)
    }
}

@Composable
private fun TaskDetailsScreenContent(
    uiState: TaskDetailsUiState,
    onTaskSubmit: (String) -> Unit,
    onFormChange: (String) -> Unit,
    onPickVideos: (WebViewPickedFile) -> Unit,
    onPickPhotos: (WebViewPickedFile) -> Unit,
    onListenersReady: () -> Unit,
    onUpdate: (WebView) -> Unit,
    onCancelWork: (String) -> Unit,
    onFileDelete: (String, String) -> Unit,
    onPreviewFile: (String) -> Unit,
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
                .wrapContentSize()
                .navigationBarsWithImePadding(),
            urlToRender = Constants.TURNIP_VIEW_URL,
            webAppInterface = WebAppInterface(
                onSubmit = { responses -> onTaskSubmit(responses) },
                onFormChange = { responses -> onFormChange(responses)},
                onListenersReady = onListenersReady,
                onPickVideos = { pickedFile -> onPickVideos(pickedFile) },
                onPickPhotos = { pickedFile -> onPickPhotos(pickedFile) },
                onFileDelete = { fieldId, filePath -> onFileDelete(fieldId, filePath) },
                onCancelWork = { fileName -> onCancelWork(fileName) },
                onPreviewFile = { storagePath -> onPreviewFile(storagePath) }
            ),
            onUpdate = onUpdate,
        )
    }
}

private fun showPreview(storagePath: String, context: Context) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setUrlBarHidingEnabled(true)
        .setShowTitle(false)
        .setShareState(SHARE_STATE_OFF)
        .build()
    FirebaseStorage.getInstance()
        .getReference(storagePath)
        .downloadUrl
        .addOnSuccessListener { uri ->
            customTabsIntent.launchUrl(context, uri)
        }
}

private fun cancelAllWork(context: Context) =
    WorkManager.getInstance(context).cancelAllWork()

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
    val prefix: String = "",
    val userId: String,
    val campaignId: String,
    val chainId: String,
    val stageId: String,
    val taskId: String
)

fun evaluateJs(webView: WebView, detail: String, eventName: String) {
    Timber.d("Event name: '$eventName'")
    webView.evaluateJavascript(
        """(function() { window.dispatchEvent(new CustomEvent('$eventName', {detail: $detail})); })();"""
    ) {}
}

private fun getRichText(text: String): String =
    JsonObject().apply { addProperty("rich_text", text) }.toString()


fun Path.getUploadPath() =
    "${this.prefix}${this.campaignId}/${this.chainId}/${this.stageId}/${this.userId}/${this.taskId}/"

data class FileProgress(
    val fileId: String,
    val fileName: String?,
    val storagePath: String?,
    val progress: Float = 0.0f,
    val workTag: String,
    val isFinished: Boolean = false
)

fun FileProgress.toJsonObject(): JsonObject = JsonObject().apply {
    addProperty("progress", progress)
    addProperty("storagePath", storagePath)
    addProperty("fileName", fileName)
    addProperty("isFinished", isFinished)
    addProperty("workTag", workTag)
}