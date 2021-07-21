package kg.kloop.android.gigaturnip.ui.tasks

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.setForceDark
import androidx.webkit.WebViewFeature
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    modifier: Modifier,
    urlToRender: String,
    payload: WebViewPayload,
    webAppInterface: WebAppInterface,
    onUpdate: () -> Unit,
    viewModel: TaskDetailsViewModel = hiltViewModel(),
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
        if (viewModel.listenersReady.value == true
            && payload.jsonSchema != null
        ) {
            val json = JsonObject().apply {
                add("jsonSchema", payload.jsonSchema.toJsonObject())
                add("uiSchema", payload.uiSchema?.toJsonObject())
                addProperty("isComplete", payload.isTaskComplete)
            }
            evaluateJs(it, json.toString(), "android_schema_event")
            evaluateJs(it, payload.formData.toString(), "android_data_event")
            evaluateJs(it, payload.fileData.toString(), "android_file_event")
        }
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

class WebAppInterface(
    private val onSubmit: (String) -> Unit,
    private val onListenersReady: () -> Unit,
    private val onPickFile: (String) -> Unit,
) {

    @JavascriptInterface
    fun onFormSubmit(data: String) {
        onSubmit(data)
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
    val jsonSchema: String?,
    val uiSchema: String?,
    val isTaskComplete: Boolean?,
    var formData: JsonObject?,
    var fileData: String?,
)

fun String.toJsonObject(): JsonObject = JsonParser().parse(this).asJsonObject