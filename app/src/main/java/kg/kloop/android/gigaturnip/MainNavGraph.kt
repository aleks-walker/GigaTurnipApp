package kg.kloop.android.gigaturnip

import android.content.Context
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
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
import kg.kloop.android.gigaturnip.util.Constants.CAMPAIGN_ID
import kg.kloop.android.gigaturnip.util.Constants.NOTIFICATION_ID
import kg.kloop.android.gigaturnip.util.Constants.STAGE_ID
import kg.kloop.android.gigaturnip.util.Constants.TASK_ID
import kotlinx.coroutines.launch


@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel,
    scaffoldState: ScaffoldState
) {
    val coroutineScope = rememberCoroutineScope()
    val openDrawer: () -> Unit = {
        coroutineScope.launch { scaffoldState.drawerState.open() }
    }
    val context = LocalContext.current

    NavHost(
        navController,
        startDestination = CampaignsScreen.CampaignScreen.route,
        modifier = modifier
    ) {
        composable(
            NotificationsScreen.NotificationsList.route.plus("/{$CAMPAIGN_ID}"),
            arguments = listOf(navArgument(CAMPAIGN_ID) { type = NavType.StringType })
        ) {
            NotificationsScreenView(
                navController = navController,
                onBack = upPress(navController)
            )
        }
        composable(
            NotificationsScreen.NotificationDetails.route.plus("/{$NOTIFICATION_ID}"),
            arguments = listOf(
                navArgument(NOTIFICATION_ID) { type = NavType.StringType })
        ) {
            NotificationDetailsScreen(onBack = upPress(navController))
        }
        composable(CampaignsScreen.CampaignScreen.route) {
            CampaignsScreenView(
                navController,
                mainActivityViewModel = viewModel,
            )
        }
        composable(CampaignsScreen.CampaignDescription.route.plus("/{$CAMPAIGN_ID}")) {
            CampaignsDescriptionScreenView(
                navController,
                mainActivityViewModel = viewModel,
            )
        }
        navigation(
            startDestination = TasksScreen.InProgress.route,
            route = TasksScreen.TasksList.route.plus("/{$CAMPAIGN_ID}")
        ) {
            composable(route = TasksScreen.InProgress.route,
                arguments = listOf(navArgument(CAMPAIGN_ID) { type = NavType.StringType })
            ) {
                TasksScreenView(
                    navigateToDetails = navigateToDetails(navController),
                    onFabClick = {
                        navController.navigate(
                            TasksScreen.Creatable.route.plus("/${viewModel.campaign.value?.id}")
                        )
                    },
                    onNotificationsClick = {
                        navController.navigate(
                            NotificationsScreen
                                .NotificationsList.route
                                .plus("/${viewModel.campaign.value?.id}")
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onLogOutClick = { logOut(context, viewModel) },
                    openDrawer = openDrawer
                )
            }
        }
        composable(
            route = TasksScreen.Details.route.plus("/{$TASK_ID}/{$STAGE_ID}"),
            arguments = listOf(
                navArgument(TASK_ID) { type = NavType.StringType },
                navArgument(STAGE_ID) { type = NavType.StringType },
            )
        ) {
            TaskDetails(onBack = upPress(navController))
        }

        composable(
            route = TasksScreen.Creatable.route.plus("/{$CAMPAIGN_ID}"),
            arguments = listOf(navArgument(CAMPAIGN_ID) { type = NavType.StringType })
        ) {
            TasksCreatable(
                navController = navController,
                onBack = upPress(navController),
                navigateToTask = navigateToDetails(navController)
            )
        }
    }

}

private fun logOut(
    context: Context,
    viewModel: MainActivityViewModel
) {
    AuthUI.getInstance().signOut(context)
    FirebaseAuth.getInstance().signOut()
    viewModel.setUser(null)
}

private fun navigateToDetails(navController: NavHostController) = { task: Task ->
    navController.navigate(
        TasksScreen.Details.route
            .plus("/${task.id}/${task.stage.id}")
    )
}
private fun upPress(navController: NavHostController): () -> Unit = { navController.navigateUp() }
