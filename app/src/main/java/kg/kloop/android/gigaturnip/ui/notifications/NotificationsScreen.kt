package kg.kloop.android.gigaturnip.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading

sealed class NotificationsScreen(val route: String) {
    object NotificationsList: NotificationsScreen("notifications_list")
    object NotificationDetails: NotificationsScreen("notification_details")
}

@Composable
fun NotificationsScreenView(
    navController: NavController,
    mainActivityViewModel: MainActivityViewModel,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val user = mainActivityViewModel.user.value
    val uiState by viewModel.uiState.collectAsState()

    user?.let {
        LoadingContent(
            empty = uiState.initialLoad,
            emptyContent = { FullScreenLoading() },
            loading = uiState.loading,
            onRefresh = { viewModel.refreshNotifications() }) {
            NotificationsScreenContent(
                navController,
                uiState.unreadNotifications,
                uiState.readNotifications
            )
        }
    }

}

@Composable
fun NotificationsScreenContent(
    navController: NavController,
    unreadNotifications: List<Notification>,
    readNotifications: List<Notification>
) {
    LazyColumn {
        items(unreadNotifications) { notification ->
            NotificationItem(notification) {
                navController.navigate(
                    NotificationsScreen.NotificationDetails.route
                        .plus("/${it.title}")
                        .plus("/${it.text}")
                )
            }
        }
        items(readNotifications) { notification ->
            NotificationItem(notification, {})
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationItemClick: (Notification) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clickable { onNotificationItemClick(notification) },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.h5
            )
            Text(
                text = notification.text,
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Composable
private fun LoadingContent(
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    loading: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
            onRefresh = onRefresh,
            content = content,
        )
    }
}

@Preview
@Composable
fun NotificationItemPreview() {
    NotificationItem(
        notification = Notification(
            "Title",
            "some text ".repeat(50),
            3
        ),
        onNotificationItemClick = {})
}