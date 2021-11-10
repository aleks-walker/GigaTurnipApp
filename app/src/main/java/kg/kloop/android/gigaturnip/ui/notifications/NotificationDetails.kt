package kg.kloop.android.gigaturnip.ui.notifications

import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kg.kloop.android.gigaturnip.ui.DetailsToolbar

@Composable
fun NotificationDetailsScreen(
    viewModel: NotificationDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    title: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            DetailsToolbar(
                title = title,
                onBack = onBack,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.h5)
            if (uiState.loading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize()
                        .padding(top = 8.dp)
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    factory = { context -> TextView(context).apply {
                        textSize = 16F
                    } },
                    update = {
                        it.text = HtmlCompat.fromHtml(
                            uiState.notification?.text.orEmpty(),
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                    }
                )
            }
        }
    }
}