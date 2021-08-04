package kg.kloop.android.gigaturnip.ui.tasks

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.util.Constants
import timber.log.Timber


@Composable
fun TaskDetails(
    navController: NavHostController,
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel = hiltViewModel()
) {

    val args = navController.currentBackStackEntry?.arguments
    val id = args?.getString("id", "No id")!!
    val user = mainActivityViewModel.user.observeAsState()



    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {

        val token = mainActivityViewModel.getUserToken().observeAsState()
        val task by viewModel.getTask(token.value.toString(), id.toInt()).observeAsState()
        TaskStageDetails(id, task?.stage)

        val fileUploadInfo by viewModel.fileUploadInfo.observeAsState()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            viewModel.uploadCompressedFiles(
                Path(
                    user.value!!.uid,
                    task!!.stage.chain.campaignId.toString(),
                    task!!.stage.chain.id.toString(),
                    task!!.id,
                    task!!.stage.id,
                ), it
            )
        }

        val listenersReady by viewModel.listenersReady.observeAsState(false)
        WebPageScreen(
            modifier = Modifier.wrapContentSize(),
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
                        token.value.toString(),
                        id.toInt(),
                        responses
                    )
                },
                onListenersReady = {
                    viewModel.setListenersReady(true)
               },
                onPickVideos = { key ->
                    launcher.launch("video/*")
                    viewModel.setPickFileKey(key)
                },
                onPickPhotos = { key ->
                    launcher.launch("image/*")
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
private fun TaskStageDetails(
    id: String,
    taskStage: TaskStage?
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
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
    TaskStageDetails(id = "123123", taskStage = null)
}

data class Path(
    val userId: String,
    val campaignId: String,
    val chainId: String,
    val stageId: String,
    val taskId: String
)
