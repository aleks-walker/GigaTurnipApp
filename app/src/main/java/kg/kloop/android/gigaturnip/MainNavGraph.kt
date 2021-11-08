package kg.kloop.android.gigaturnip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsDescriptionScreenView
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsScreen
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsScreenView
import kg.kloop.android.gigaturnip.ui.notifications.NotificationDetailsScreen
import kg.kloop.android.gigaturnip.ui.notifications.NotificationsScreen
import kg.kloop.android.gigaturnip.ui.notifications.NotificationsScreenView
import kg.kloop.android.gigaturnip.ui.tasks.screens.TaskDetails
import kg.kloop.android.gigaturnip.ui.tasks.screens.TasksCreatable
import kg.kloop.android.gigaturnip.ui.tasks.screens.TasksScreen
import kg.kloop.android.gigaturnip.ui.tasks.screens.TasksScreenView


@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel
) {
    NavHost(
        navController,
        startDestination = CampaignsScreen.CampaignScreen.route,
        modifier = modifier
    ) {
        composable(NotificationsScreen.NotificationsList.route) {
            NotificationsScreenView(
                navController = navController,
                mainActivityViewModel = viewModel
            )
        }
        composable(
            NotificationsScreen.NotificationDetails.route.plus("/{id}/{title}/{text}"),
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("text") { type = NavType.StringType })
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val title = args?.getString("title").toString()
            val text = args?.getString("text").toString()
            NotificationDetailsScreen(title = title, text = text)
        }
        composable(CampaignsScreen.CampaignScreen.route) {
            CampaignsScreenView(
                navController,
                mainActivityViewModel = viewModel,
            )
        }
        composable(CampaignsScreen.CampaignDescription.route) {
            CampaignsDescriptionScreenView(
                navController,
                mainActivityViewModel = viewModel,
            )
        }
        navigation(
            startDestination = TasksScreen.InProgress.route,
            route = TasksScreen.TasksList.route
        ) {
            composable(route = TasksScreen.InProgress.route) {
                TasksScreenView(
                    mainActivityViewModel = viewModel,
                    navigateToDetails = navigateToDetails(navController),
                    onFabClick = {
                        navController.navigate(
                            TasksScreen.Creatable.route.plus("/${viewModel.campaign.value?.id}")
                        )
                    },
                )
            }
        }
        composable(
            route = TasksScreen.Details.route.plus("/{id}/{stage_id}"),
            arguments = listOf(navArgument("id") { type = NavType.StringType },
                navArgument("stage_id") { type = NavType.StringType })
        ) {
            TaskDetails(
                navController = navController,
                mainActivityViewModel = viewModel
            )
        }

        composable(
            route = TasksScreen.Creatable.route.plus("/{campaign_id}"),
            arguments = listOf(navArgument("campaign_id") { type = NavType.StringType })
        ) { TasksCreatable(navController = navController) }
    }

}

private fun navigateToDetails(navController: NavHostController) = { task: Task ->
    navController.navigate(
        TasksScreen.Details.route
            .plus("/${task.id}/${task.stage.id}")
    )
}
