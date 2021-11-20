package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.tasks.screens.TasksScreen


sealed class CampaignsScreen(val route: String) {
    object CampaignScreen : CampaignsScreen("campaigns_list")
    object CampaignDescription: CampaignsScreen("campaign_description")
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

        CampaignsScreenContent(
            uiState.campaigns,
            uiState.selectableCampaigns,
            onCampaignClick = { campaign ->
                mainActivityViewModel.setCampaign(campaign)
                navController.navigate(TasksScreen.TasksList.route.plus("/${campaign.id}"))
            },
            onSelectableCampaignClick = { campaign ->
                mainActivityViewModel.setCampaign(campaign)
                navController.navigate(CampaignsScreen.CampaignDescription.route.plus("/${campaign.id}"))
            }
        )
    }

}

@Composable
fun CampaignsDescriptionScreenView(
    navController: NavHostController,
    mainActivityViewModel: MainActivityViewModel,
    viewModel: CampaignDescriptionViewModel = hiltViewModel()
) {
    val campaign = mainActivityViewModel.campaign.value!!
    val uiState by viewModel.uiState.collectAsState()
    viewModel.setCampaign(campaign = campaign)

    if (uiState.joined) {
        navigateToTasks(navController)
        viewModel.setJoined(false)
    } else {
        CampaignDescriptionContent(uiState) { campaignId ->
            viewModel.joinCampaign(campaignId)
        }
    }
}

@Composable
private fun CampaignDescriptionContent(
    uiState: CampaignDescriptionUiState,
    onJoinCampaignClick: (String) -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = uiState.campaign?.description.orEmpty())
        Button(
            enabled = !uiState.loading,
            onClick = {
                onJoinCampaignClick(uiState.campaign!!.id)
            }) {
            if (uiState.loading) CircularProgressIndicator(modifier = Modifier.wrapContentSize())
            else Text(text = context.getString(R.string.join))
        }
    }
}

private fun navigateToTasks(
    navController: NavHostController,
) {
    navController.navigate(TasksScreen.TasksList.route) {
        popUpTo(CampaignsScreen.CampaignScreen.route)
    }
}

@Composable
private fun CampaignsScreenContent(
    campaigns: List<Campaign>,
    selectableCampaigns: List<Campaign>,
    onCampaignClick: (Campaign) -> Unit,
    onSelectableCampaignClick: (Campaign) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = stringResource(id = R.string.choose_campaign),
                        style = MaterialTheme.typography.h5
                    )
                })
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            items(campaigns) {
                CampaignItem(
                    campaign = it,
                    onClick = { onCampaignClick(it) }
                )
            }
            items(selectableCampaigns) {
                CampaignItem(
                    campaign = it,
                    onClick = { onSelectableCampaignClick(it) }
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
