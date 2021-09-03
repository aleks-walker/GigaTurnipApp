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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.placeholder
import com.google.firebase.auth.FirebaseUser
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.ui.tasks.TaskDetailsViewModel
import kg.kloop.android.gigaturnip.ui.tasks.WebAppInterface
import kg.kloop.android.gigaturnip.ui.tasks.WebPageScreen
import kg.kloop.android.gigaturnip.ui.tasks.WebViewPayload
import kg.kloop.android.gigaturnip.util.Constants
import timber.log.Timber

@Composable
fun TaskDetails(
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel
) {

    val taskId = viewModel.taskId
    val user = mainActivityViewModel.user.observeAsState()
    val isTaskLoading by viewModel.isTaskLoading.observeAsState(true)

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val token = mainActivityViewModel.getUserToken().observeAsState()
        val task by viewModel.getTask(token.value.toString(), taskId.toInt()).observeAsState()
        TaskStageDetails(taskId, task?.stage, isTaskLoading)

        val fileUploadInfo by viewModel.fileUploadInfo.observeAsState()

        val videoLauncher = getActivityLauncher(
            { path, urls -> viewModel.uploadCompressedFiles(path, urls) }, user, task
        )
        val photoLauncher = getActivityLauncher(
            { path, urls -> viewModel.uploadFiles(path, urls) }, user, task
        )

        val listenersReady by viewModel.listenersReady.observeAsState(false)

        val isTaskCompleted by viewModel.isTaskCompleted.observeAsState(false)
        if (isTaskCompleted) { showTaskCompletedToast() }

        WebPageScreen(
            modifier = Modifier
                .wrapContentSize()
                .placeholder(
                    visible = isTaskLoading,
                    highlight = PlaceholderHighlight.fade(),
                    color = PlaceholderDefaults.color()
                ),
            urlToRender = Constants.TURNIP_VIEW_URL,
            payload = WebViewPayload(
                jsonSchema = task?.stage?.jsonSchema,
                uiSchema = task?.stage?.uiSchema,
                isTaskComplete = task?.isComplete,
                formData = task?.responses,
                fileData = fileUploadInfo
            ),
            webAppInterface = WebAppInterface(
                onSubmit= { responses ->
                    viewModel.updateTask(
                        token = token.value.toString(),
                        id = taskId.toInt(),
                        responses = responses,
                        complete = true
                    )
                },
                onListenersReady = {
                    viewModel.setListenersReady(true)
               },
                onPickVideos = { key ->
                    videoLauncher.launch("video/*")
                    viewModel.setPickFileKey(key)
                },
                onPickPhotos = { key ->
                    photoLauncher.launch("image/*")
                    viewModel.setPickFileKey(key)
                }
            ),
            onUpdate = {
                if (listenersReady) {
                    Timber.d("onUpdate listeners: $listenersReady")
                }
            },
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
    onActivityResult: (Path, List<Uri>) -> Unit,
    user: State<FirebaseUser?>,
    task: Task?
) = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
    onActivityResult(
        Path(
            user.value!!.uid,
            task!!.stage.chain.campaignId.toString(),
            task!!.stage.chain.id.toString(),
            task!!.id,
            task!!.stage.id,
        ), it
    )
}

@Composable
private fun TaskStageDetails(
    id: String,
    taskStage: TaskStage?,
    isTaskLoading: Boolean
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .placeholder(
                visible = isTaskLoading,
                highlight = PlaceholderHighlight.fade(),
                color = PlaceholderDefaults.color()
            )
        ,
    ) {
        Text(
            text = taskStage?.name.toString(),
            style = MaterialTheme.typography.h4
        )
        SelectionContainer {
            Text(
                modifier = Modifier,
                text = "id: $id",
                style = MaterialTheme.typography.subtitle2,
                color = Color.LightGray
            )
        }
        Text(
            text = taskStage?.description.toString(),
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview(showBackground = false)
@Composable
fun TaskStageDetailPreview() {
    TaskStageDetails(id = "123123", taskStage = null, false)
}

data class Path(
    val userId: String,
    val campaignId: String,
    val chainId: String,
    val stageId: String,
    val taskId: String
)
