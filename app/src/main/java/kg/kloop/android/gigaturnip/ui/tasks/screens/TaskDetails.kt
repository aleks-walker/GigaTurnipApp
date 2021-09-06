package kg.kloop.android.gigaturnip.ui.tasks.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.tasks.TaskDetailsUiState
import kg.kloop.android.gigaturnip.ui.tasks.TaskDetailsViewModel
import kg.kloop.android.gigaturnip.ui.tasks.WebAppInterface
import kg.kloop.android.gigaturnip.ui.tasks.WebPageScreen
import kg.kloop.android.gigaturnip.util.Constants

@Composable
fun TaskDetails(
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel
) {

    val uiState by viewModel.uiState.collectAsState()
    val user = mainActivityViewModel.user.observeAsState()

    LoadingContent(
        empty = uiState.initialLoad,
        emptyContent = { FullScreenLoading() },
        loading = uiState.loading,
        onRefresh = { viewModel.refreshTaskDetails() }) {
        if (uiState.task != null && user.value != null) {
            val videoLauncher = getActivityLauncher { urls ->
                viewModel.uploadCompressedFiles(getPath(user.value!!.uid, uiState.task!!), urls)
            }
            val photoLauncher = getActivityLauncher { urls ->
                viewModel.uploadFiles(getPath(user.value!!.uid, uiState.task!!), urls)
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

private fun getPath(
    userId: String,
    task: Task
) = Path(
    userId,
    task.stage.chain.campaignId.toString(),
    task.stage.chain.id.toString(),
    task.id,
    task.stage.id,
)

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
