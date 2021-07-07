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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.domain.TaskStage
import timber.log.Timber
import kotlin.random.Random

@Composable
fun TaskDetails(
    navController: NavHostController,
    viewModel: TaskDetailsViewModel = hiltViewModel()
) {

    val args = navController.currentBackStackEntry?.arguments
    val id = args?.getString("id", "No id")!!
    val stageId = args.getString("stage_id", "No stage id")!!


    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {

        val taskStage by viewModel.getTaskStage(stageId.toInt()).observeAsState()
        TaskStageDetails(stageId, taskStage)

        val originalFileUri = remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            originalFileUri.value = it
        }
        val fileDestination by viewModel.compressedFilePath.observeAsState()
        val compressionProgress: Int by viewModel.compressionProgress.observeAsState(0)
//        Compress(launcher, originalFileUri, compressionProgress, viewModel, fileDestination)

        val formData by viewModel.formData.observeAsState("Initial")
        Text(text = formData)
        Button(
            onClick = {
                Timber.d("form data before update: ${viewModel.formData.value}")
                Timber.d("json schema before update: ${taskStage?.jsonSchema}")
                if (viewModel.formData.value == null) {
                    viewModel.postFormData("Updated value: ${Random.nextInt(10)}")
                } else {
                    viewModel.postFormData(viewModel.formData.value!!)
                }
            }) {
            Text(text = "Show form")
        }
        Timber.d("webview payload: ${taskStage?.jsonSchema.toString()}")
        WebPageScreen(
            modifier = Modifier.wrapContentSize(),
            urlToRender = "http://10.0.2.2:3000/",
            payload = WebViewPayload(
                jsonSchema = taskStage?.jsonSchema.toString(),
                uiSchema = taskStage?.uiSchema.toString(),
                formData = formData,
            ),
            webAppInterface = WebAppInterface(
                onValueChange = { viewModel.postFormData(it) },
                launcher = launcher
            )
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