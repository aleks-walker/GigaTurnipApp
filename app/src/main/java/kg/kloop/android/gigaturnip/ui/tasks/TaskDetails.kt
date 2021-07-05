package kg.kloop.android.gigaturnip.ui.tasks

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.domain.TaskStage
import timber.log.Timber

@Composable
fun TaskDetails(
    navController: NavHostController,
    viewModel: TaskDetailsViewModel = hiltViewModel()
) {

    val args = navController.currentBackStackEntry?.arguments
    val id = args?.getString("id", "No id")!!
    val title = args.getString("title", "No title")!!
    val description = args.getString("description", "No description")!!


    Column(
        modifier = Modifier
            .padding(16.dp)
            .size(1500.dp),
//            .fillMaxSize(),
//            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val taskStage by viewModel.getTaskStage(id.toInt()).observeAsState()

        TaskStageDetails(id, taskStage)

        val result = remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            result.value = it
        }
        val fileDestination by viewModel.compressedFilePath.observeAsState()
        val compressionProgress: Int by viewModel.compressionProgress.observeAsState(0)
        Compress(launcher, result, compressionProgress, viewModel, fileDestination)

        val formData by viewModel.formData.observeAsState("Initial")
        Text(text = formData)
        WebPageScreen(
            modifier = Modifier.size(500.dp),
            urlToRender = "http://10.0.2.2:3000/",
            jsonSchema = taskStage?.jsonSchema.toString(),
            uiSchema = taskStage?.uiSchema.toString(),
            formData = formData,
            launcher = launcher,
            onValueChange = { viewModel.postFormData(it) })

    }
}

@Composable
private fun Compress(
    launcher: ManagedActivityResultLauncher<String, Uri>,
    result: MutableState<Uri?>,
    compressionProgress: Int,
    viewModel: TaskDetailsViewModel,
    fileDestination: String?
) {
    Button(modifier = Modifier
        .padding(16.dp),
        onClick = { launcher.launch("video/*") }) {
        Text(text = "Compress")
    }
    result.value?.let { uri ->
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
    Text(text = id, style = MaterialTheme.typography.caption)
    Text(
        modifier = Modifier.padding(16.dp),
        text = taskStage?.name.toString(),
        style = MaterialTheme.typography.h5
    )
    Text(text = taskStage?.description.toString(), style = MaterialTheme.typography.subtitle2)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    modifier: Modifier,
    urlToRender: String,
    jsonSchema: String,
    uiSchema: String,
    formData: String,
    onValueChange: (String) -> Unit,
    launcher: ManagedActivityResultLauncher<String, Uri>
) {
    Timber.d("schemas sent: $jsonSchema, $uiSchema, $formData")
    AndroidView(modifier = modifier, factory = { context ->
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {

                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    Log.d("MyApplication", "${message.message()} -- From line " +
                            "${message.lineNumber()}")
                    return true
                }
            }
            addJavascriptInterface(
                WebAppInterface(
                    onValueChange = onValueChange,
                    launcher = launcher
                ), "Android"
            )
            loadUrl(urlToRender)
            Timber.d("evaluate after load")
            evaluateJavascript(
                "(function() { window.dispatchEvent(new CustomEvent('android_formdata_event', {formData: '$formData'})); })();") {}
        }
    }, update = {
        Timber.d("formData: $formData")
        it.evaluateJavascript(
            "(function() { window.dispatchEvent(new CustomEvent('android_formdata_event', {formData: '$formData'})); })();"
//            """(function() { window.dispatchEvent(new CustomEvent('android_formdata_event',
//                {
//                    formData: '$formData',
//                    jsonSchema: '$jsonSchema',
//                    uiSchema: '$uiSchema',
//                })); })();""".trimMargin()
        ) { Timber.d("evaluate javascrtipt")}
        Timber.d("update javascrtipt")
    })
}

class WebAppInterface(
    private val onValueChange: (String) -> Unit,
    private val launcher: ManagedActivityResultLauncher<String, Uri>
) {

    @JavascriptInterface
    fun setFormData(data: String) {
        Timber.d("set form data: $data")
        onValueChange(data)
    }

    @JavascriptInterface
    fun pickVideo() {
        Timber.d("pick video")
        launcher.launch("video/*")
    }
}
