package kg.kloop.android.gigaturnip.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NotificationDetailsScreen(
    title: String,
    text: String
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

@Preview
@Composable
fun NotificationDetailsScreenPreview() {
    NotificationDetailsScreen(title = "Title", text = "text ".repeat(50))
}