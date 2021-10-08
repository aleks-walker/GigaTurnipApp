package kg.kloop.android.gigaturnip.ui.tasks

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.setForceDark
import androidx.webkit.WebViewFeature
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kg.kloop.android.gigaturnip.util.Constants.DATA_EVENT
import kg.kloop.android.gigaturnip.util.Constants.FILE_EVENT
import kg.kloop.android.gigaturnip.util.Constants.PREVIOUS_TASKS_EVENT
import kg.kloop.android.gigaturnip.util.Constants.RICH_TEXT_EVENT
import kg.kloop.android.gigaturnip.util.Constants.SCHEMA_EVENT
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    modifier: Modifier,
    urlToRender: String,
    webAppInterface: WebAppInterface,
    onUpdate: () -> Unit,
    uiState: TaskDetailsUiState
) {
    AndroidView(modifier = modifier, factory = { context ->
        WebView(context).apply {
            layoutParams = layoutParams()
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = webChromeClient()
            addJavascriptInterface(webAppInterface, "Android")
            loadUrl(urlToRender)
//            setDarkMode(this)

        }
    }, update = {
        onUpdate()
        val json = JsonObject().apply {
            add("jsonSchema", uiState.task!!.stage.jsonSchema.toJsonObject())
            add("uiSchema", uiState.task.stage.uiSchema.toJsonObject())
            addProperty("isComplete", uiState.task.isComplete)
        }
        evaluateJs(it, uiState.task!!.stage.richText.toString(), RICH_TEXT_EVENT)
        evaluateJs(it, uiState.previousTasks.toString(), PREVIOUS_TASKS_EVENT)
        evaluateJs(it, json.toString(), SCHEMA_EVENT)
        evaluateJs(it, uiState.task.responses.toString(), DATA_EVENT)
        evaluateJs(it, uiState.fileProgressState.toString(), FILE_EVENT)
    })
}


private fun setDarkMode(webView: WebView) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
    }
}

private fun layoutParams() = ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT
)

private fun webChromeClient() = object : WebChromeClient() {
    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        Timber.d(
            """MyApplication ${message.message()} 
             -- From line ${message.lineNumber()}""".trimMargin()
        )
        return true
    }
}

private fun evaluateJs(webView: WebView, detail: String, eventName: String) {
    Timber.d("Event name: '$eventName'")
    webView.evaluateJavascript(
        "(function() { window.dispatchEvent(new CustomEvent(\'$eventName\', " +
                "{detail: '$detail'})); })();"
    ) {}
}

data class WebViewPickedFile(
    val key: String,
    val isPrivate: Boolean = true
)

class WebAppInterface(
    private val onSubmit: (String) -> Unit,
    private val onFormChange: (String) -> Unit,
    private val onListenersReady: () -> Unit,
    private val onPickVideos: (WebViewPickedFile) -> Unit,
    private val onPickPhotos: (WebViewPickedFile) -> Unit,
    private val onFileDelete: (String) -> Unit,
    private val onCancelWork: (String) -> Unit,
    private val onPreviewFile: (String) -> Unit,
) {

    @JavascriptInterface
    fun onFormSubmit(data: String) {
        onSubmit(data)
    }

    @JavascriptInterface
    fun onChange(data: String) {
        onFormChange(data)
    }

    @JavascriptInterface
    fun pickVideos(key: String, isPrivate: Boolean) {
        onPickVideos(WebViewPickedFile(key, isPrivate))
    }

    @JavascriptInterface
    fun pickPhotos(key: String, isPrivate: Boolean) {
        onPickPhotos(WebViewPickedFile(key, isPrivate))
    }

    @JavascriptInterface
    fun listenersReady(){
        onListenersReady()
    }

    @JavascriptInterface
    fun deleteFile(filePath: String){
        onFileDelete(filePath)
    }

    @JavascriptInterface
    fun cancelWork(fileName: String){
        onCancelWork(fileName)
    }

    @JavascriptInterface
    fun previewFile(downloadUrl: String){
        onPreviewFile(downloadUrl)
    }
}

fun String.toJsonObject(): JsonObject = JsonParser().parse(this).asJsonObject