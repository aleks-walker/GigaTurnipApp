package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.ui.tasks.TasksScreen


sealed class CampaignsScreen(val route: String) {
    object CampaignScreen : CampaignsScreen("campaigns_list")
}

@Composable
fun CampaignsScreenView(navController: NavHostController) {
    val viewModel = CampaignsViewModel()
    val campaigns: List<Campaign> by viewModel.getCampaigns().observeAsState(listOf())
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        items(campaigns) {
            CampaignItem(
                campaign = it,
                onClick = { navController.navigate(TasksScreen.TasksList.route) })
        }

    }

}

@Composable
fun CampaignItem(campaign: Campaign, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .height(150.dp)
            .padding(4.dp),
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