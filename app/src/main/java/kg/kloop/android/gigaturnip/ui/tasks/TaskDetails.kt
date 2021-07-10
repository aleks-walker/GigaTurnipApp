package kg.kloop.android.gigaturnip.ui.tasks

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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

//        val originalFileUri = remember { mutableStateOf<Uri?>(null) }
//        val pickFileKey by viewModel.pickFileKey.observeAsState()
        val fileUploadInfo by viewModel.fileUploadInfo.observeAsState()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
//            originalFileUri.value = it
            viewModel.uploadFiles(
                Path(
                    user.value!!.uid,
                    "1",
                    task!!.stage.chain.toString(),
                    task!!.id,
                    task!!.stage.id,
                ), it
            )
        }
        val fileDestination by viewModel.compressedFilePath.observeAsState()
        val compressionProgress: Int by viewModel.compressionProgress.observeAsState(0)
//        Compress(launcher, originalFileUri, compressionProgress, viewModel, fileDestination)

//        val formData by viewModel.formData.observeAsState("{}")
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
                onValueChange = { responses ->
                    viewModel.updateTask(
                        token.value.toString(),
                        id.toInt(),
                        responses
                    )
                },
                onListenersReady = {
                    viewModel.setListenersReady(true)
               },
                onPickFile = { key ->
                    launcher.launch("*/*")
                    viewModel.setPickFileKey(key)
                }
            ),
            onUpdate = {
                if (listenersReady) {
//                    viewModel.setListenersReady(false)
                    Timber.d("onUpdate listeners: $listenersReady")
                }
            },
        )
    }
}


@Composable
private fun Compress(
    launcher: ManagedActivityResultLauncher<String, Uri>,
    originalFileUri: MutableState<Uri?>,
    compressionProgress: Int,
    viewModel: TaskDetailsViewModel,
    fileDestination: String?
) {
    Button(modifier = Modifier
        .padding(16.dp),
        onClick = { launcher.launch("video/*") }) {
        Text(text = "Compress")
    }
    originalFileUri.value?.let { uri ->
        Timber.d("compression function called")
        if (compressionProgress == 0) viewModel.compressInTheBackground(uri, "")
        Column() {
            Text(text = uri.toString())
            Text(text = fileDestination ?: "Compressing . . .")
            Text(text = "Progress: $compressionProgress%")
        }
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
