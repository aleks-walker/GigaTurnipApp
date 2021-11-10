package kg.kloop.android.gigaturnip.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.theme.DarkBlue900
import kg.kloop.android.gigaturnip.ui.theme.LightBlue500

sealed class NotificationsScreen(val route: String) {
    object NotificationsList: NotificationsScreen("notifications_list")
    object NotificationDetails: NotificationsScreen("notification_details")
}

@Composable
fun NotificationsScreenView(
    navController: NavController,
    mainActivityViewModel: MainActivityViewModel,
    viewModel: NotificationsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val user = mainActivityViewModel.user.value
    val uiState by viewModel.uiState.collectAsState()
    viewModel.setCampaignId(mainActivityViewModel.campaign.value!!.id)

    user?.let {
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
                NotificationsScreenContent(
                    navController,
                    uiState.unreadNotifications,
                    uiState.readNotifications,
                    onRefresh = { viewModel.refreshNotifications() }
                )
            }

        }
    }
}

@Composable
fun NotificationsScreenContent(
    navController: NavController,
    unreadNotifications: List<Notification>,
    readNotifications: List<Notification>,
    onRefresh: () -> Unit
) {
    if (unreadNotifications.isEmpty() && readNotifications.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.nothing_found))
            Button(
                onClick = { onRefresh() },
            ) {
                Text(
                    text = stringResource(id = R.string.try_again),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            items(unreadNotifications) { notification ->
                NotificationItem(notification) {

                    navigateToNotification(navController, it)
                }
            }
            items(readNotifications) { notification ->
                NotificationItem(notification, read = true) {
                    navigateToNotification(navController, it)
                }
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
            .plus("/${it.title}")
    )
}

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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.h5
                    )
                    Text(
                        text = HtmlCompat.fromHtml(
                            notification.text,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toString(),
                        style = MaterialTheme.typography.caption,
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
            title = "title",
            text = "text ".repeat(50),
            importance = 3,
            createdAt = "asdf",
            updatedAt = "asdf",
            campaignId = 1,
            rankId = 1
        ),
        read = true,
        onNotificationItemClick = {})
}