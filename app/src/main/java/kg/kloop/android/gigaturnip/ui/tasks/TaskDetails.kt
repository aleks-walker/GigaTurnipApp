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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
            .fillMaxSize(),
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
            urlToRender = "",
            formData = formData,
            onValueChange = { viewModel.setFormData(it) })

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
fun WebPageScreen(urlToRender: String, formData: String, onValueChange : (String) -> Unit) {
    AndroidView(factory = {context ->
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
            addJavascriptInterface(WebAppInterface(onValueChange = onValueChange), "Android")
            loadUrl(urlToRender)
        }
    }, update = {
        it.evaluateJavascript(
            "(function() { window.dispatchEvent(new CustomEvent('android_formdata_event', {detail: '$formData'})); })();"
        ) { }
    })
}

class WebAppInterface(private val onValueChange : (String) -> Unit) {

    @JavascriptInterface
    fun setFormData(toast: String) {
        onValueChange(toast)
    }
}
