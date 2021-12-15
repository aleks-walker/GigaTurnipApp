package kg.kloop.android.gigaturnip.ui.tasks

import android.annotation.SuppressLint
import android.view.View
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
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    modifier: Modifier,
    urlToRender: String,
    webAppInterface: WebAppInterface,
    onUpdate: (WebView) -> Unit,
) {
    AndroidView(modifier = modifier, factory = { context ->
        WebView(context).apply {
            layoutParams = layoutParams()
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = webChromeClient()
            addJavascriptInterface(webAppInterface, "Android")
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(urlToRender)
//            setDarkMode(this)

        }
    }, update = { onUpdate(it) }) }

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
    private val onFileDelete: (String, String) -> Unit,
    private val onCancelWork: (String) -> Unit,
    private val onPreviewFile: (String) -> Unit,
    private val onGoToPreviousTask: () -> Unit,
    private val onGoToRecordAudio: (WebViewPickedFile) -> Unit
) {

    @JavascriptInterface
    fun onFormSubmit(data: String) {
        Timber.d("CALLBACK onFromSubmit: $data")
        onSubmit(data)
    }

    @JavascriptInterface
    fun onGoToPrevious() {
        Timber.d("CALLBACK onGoPrevious")
        onGoToPreviousTask()
    }

    @JavascriptInterface
    fun onChange(data: String) {
        Timber.d("CALLBACK onChange: $data")
        onFormChange(data)
    }

    @JavascriptInterface
    fun pickVideos(key: String, isPrivate: Boolean) {
        Timber.d("CALLBACK pickVideos")
        onPickVideos(WebViewPickedFile(key, isPrivate))
    }

    @JavascriptInterface
    fun pickPhotos(key: String, isPrivate: Boolean) {
        Timber.d("CALLBACK pickPhotos")
        onPickPhotos(WebViewPickedFile(key, isPrivate))
    }

    @JavascriptInterface
    fun listenersReady(){
        onListenersReady()
    }

    @JavascriptInterface
    fun deleteFile(fieldId: String, filePath: String){
        Timber.d("CALLBACK onFileDelete")
        onFileDelete(fieldId, filePath)
    }

    @JavascriptInterface
    fun cancelWork(fileName: String){
        Timber.d("CALLBACK cancelWork")
        onCancelWork(fileName)
    }

    @JavascriptInterface
    fun previewFile(downloadUrl: String){
        onPreviewFile(downloadUrl)
    }

    @JavascriptInterface
    fun recordAudio(key: String, isPrivate: Boolean) {
        onGoToRecordAudio(WebViewPickedFile(key, isPrivate))
    }
}

fun String?.toJsonObject(): JsonObject = if (this.isNullOrBlank() || this == "null") JsonObject()
    else JsonParser().parse(this).asJsonObject