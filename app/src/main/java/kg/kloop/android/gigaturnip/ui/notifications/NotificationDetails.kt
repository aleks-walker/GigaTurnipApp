package kg.kloop.android.gigaturnip.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kg.kloop.android.gigaturnip.ui.DetailsToolbar

@Composable
fun NotificationDetailsScreen(
    viewModel: NotificationDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    title: String,
    text: String
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .wrapContentSize()
                .padding(8.dp)) {
                Text(text = title, style = MaterialTheme.typography.h5)
                Text(text = text, style = MaterialTheme.typography.body1)
            }
        }
    }
}