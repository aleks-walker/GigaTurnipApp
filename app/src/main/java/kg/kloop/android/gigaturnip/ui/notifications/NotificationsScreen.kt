package kg.kloop.android.gigaturnip.ui.notifications

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.components.TryAgainScreen
import kg.kloop.android.gigaturnip.ui.theme.DarkBlue900
import kg.kloop.android.gigaturnip.ui.theme.LightBlue500
import kg.kloop.android.gigaturnip.util.toTimeAgoFormat

sealed class NotificationsScreen(val route: String) {
    object NotificationsList: NotificationsScreen("notifications_list")
    object NotificationDetails: NotificationsScreen("notification_details")
}

@Composable
fun NotificationsScreenView(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    refreshOnce(viewModel)

    Scaffold(
        topBar = {
            DetailsToolbar(
                title = stringResource(id = R.string.notifications),
                onBack = onBack,
            )
        },
    ) {
        LoadingContent(
            empty = uiState.initialLoad,
            emptyContent = { FullScreenLoading() },
            loading = uiState.loading,
            onRefresh = { viewModel.refreshNotifications() }) {
            if (uiState.error) {
                TryAgainScreen { viewModel.refreshNotifications() }
            } else {
                NotificationsScreenContent(
                    navController,
                    uiState.unreadNotifications,
                    uiState.readNotifications,
                )
            }
        }
    }
}

@Composable
private fun refreshOnce(viewModel: NotificationsViewModel) {
    var refreshing by remember { mutableStateOf(true) }
    if (refreshing) {
        viewModel.refreshNotifications()
        refreshing = false
    }
}

@Composable
fun NotificationsScreenContent(
    navController: NavController,
    unreadNotifications: List<Notification>,
    readNotifications: List<Notification>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        items(unreadNotifications.sortedBy { it.createdAt }.reversed()) { notification ->
            NotificationItem(notification) {
                navigateToNotification(navController, it)
            }
        }
        items(readNotifications.sortedBy { it.createdAt }.reversed()) { notification ->
            NotificationItem(notification, read = true) {
                navigateToNotification(navController, it)
            }
        }
    }
}

private fun navigateToNotification(
    navController: NavController,
    it: Notification
) {
    navController.navigate(
        NotificationsScreen.NotificationDetails.route
            .plus("/${it.id}")
    )
}

@SuppressLint("SimpleDateFormat")
@Composable
fun NotificationItem(
    notification: Notification,
    read: Boolean = false,
    onNotificationItemClick: (Notification) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clickable { onNotificationItemClick(notification) },
        shape = MaterialTheme.shapes.medium,
        backgroundColor = if (read) DarkBlue900 else LightBlue500
    ) {
        val context = LocalContext.current
        Row(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.h5,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.createdAt.toTimeAgoFormat(),
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = HtmlCompat.fromHtml(
                        notification.text,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    ).toString(),
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!read) {
                Text(
                    text = context.getString(R.string.word_new).lowercase(),
                    color = Color.Yellow
                )
            }
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

@Preview(showSystemUi = false, showBackground = false)
@Composable
fun NotificationItemPreview() {
    NotificationItem(
        notification = Notification(
            id = 1,
            title = "title".repeat(50),
            text = "text ".repeat(50),
            importance = 3,
            createdAt = "asdf",
            updatedAt = "asdf",
            campaignId = 1,
            rankId = 1
        ),
        read = false,
        onNotificationItemClick = {})
}