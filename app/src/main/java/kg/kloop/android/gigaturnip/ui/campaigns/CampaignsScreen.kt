package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.tasks.screens.TasksScreen


sealed class CampaignsScreen(val route: String) {
    object CampaignScreen : CampaignsScreen("campaigns_list")
}

@Composable
fun CampaignsScreenView(
    navController: NavHostController,
    mainActivityViewModel: MainActivityViewModel,
    viewModel: CampaignsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LoadingContent(
        empty = uiState.initialLoad,
        emptyContent = { FullScreenLoading() },
        loading = uiState.loading,
        onRefresh = { viewModel.refreshCampaigns() }) {

        CampaignsScreenContent(uiState.campaigns) { campaignId ->
            mainActivityViewModel.setCampaignId(campaignId)
            navController.navigate(TasksScreen.TasksList.route)
        }
    }

}

@Composable
private fun CampaignsScreenContent(
    campaigns: List<Campaign>,
    onCampaignClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        items(campaigns) {
            CampaignItem(
                campaign = it,
                onClick = { onCampaignClick(it.id) }
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

@Composable
fun CampaignItem(campaign: Campaign, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .height(150.dp)
            .padding(start = 8.dp, top = 8.dp, end = 8.dp),
        elevation = 3.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = campaign.title,
            modifier = Modifier
                .wrapContentSize(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4
        )
    }

}


@Preview(showBackground = true)
@Composable
fun CampaignItemPreview() {
    CampaignItem(campaign = Campaign("123", "Title", "Description")) {}
}