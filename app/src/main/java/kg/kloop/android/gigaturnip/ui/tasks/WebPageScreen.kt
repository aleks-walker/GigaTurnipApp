package kg.kloop.android.gigaturnip.ui.tasks

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    modifier: Modifier,
    urlToRender: String,
    payload: WebViewPayload,
    webAppInterface: WebAppInterface,
    onUpdate: () -> Unit,
) {
    AndroidView(modifier = modifier, factory = { context ->
        WebView(context).apply {
            layoutParams = layoutParams()
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = webChromeClient()
            addJavascriptInterface(webAppInterface, "Android")
            loadUrl(urlToRender)
        }
    }, update = {
        onUpdate()
        evaluateJs(it, payload.jsonSchema, "android_json_event")
        evaluateJs(it, payload.uiSchema, "android_ui_event")
        evaluateJs(it, payload.formData, "android_data_event")
        evaluateJs(it, payload.fileData, "android_file_event")
    })
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

class WebAppInterface(
    private val onValueChange: (String) -> Unit,
    private val onListenersReady: () -> Unit,
    private val onPickFile: (String) -> Unit,
) {

    @JavascriptInterface
    fun setFormData(data: String) {
        onValueChange(data)
    }

    @JavascriptInterface
    fun pickFile(key: String) {
        onPickFile(key)
    }

    @JavascriptInterface
    fun listenersReady(){
        onListenersReady()
    }
}

data class WebViewPayload(
    val jsonSchema: String,
    val uiSchema: String,
    var formData: String,
    var fileData: String
)
