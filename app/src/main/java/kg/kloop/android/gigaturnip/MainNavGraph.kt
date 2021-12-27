package kg.kloop.android.gigaturnip

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.firebase.ui.auth.AuthUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.firebase.auth.FirebaseAuth
import kg.kloop.android.gigaturnip.ui.audiorecording.RecordAudioScreen
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
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_KEY
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.CAMPAIGN_ID
import kg.kloop.android.gigaturnip.util.Constants.NOTIFICATION_ID
import kg.kloop.android.gigaturnip.util.Constants.TASK_ID


@ExperimentalPermissionsApi
@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel,
) {
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
                    mainActivityViewModel = viewModel,
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
                )
            }
        }
        composable(
            route = TasksScreen.Details.route.plus("/{$TASK_ID}/{$AUDIO_FILE_KEY}/{$AUDIO_FILE_UPLOAD_PATH}"),
            arguments = listOf(
                navArgument(TASK_ID) { type = NavType.StringType },
            )
        ) {
            TaskDetails(
                onBack = upPress(navController),
                navigateToTask = navigateToDetails(navController),
                navigateToAudioRecording = { navController.navigate(TaskDetails.RecordAudio.route.plus("/{AUDIO_FILE_UPLOAD_PATH}")) }
            )
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

        composable(TaskDetails.RecordAudio.route.plus("/{AUDIO_FILE_UPLOAD_PATH}"),
            arguments = listOf(
//                navArgument(AUDIO_FILE_KEY) { type = NavType.StringType },
                navArgument(AUDIO_FILE_UPLOAD_PATH) { type = NavType.StringType })
        ) {
            RecordAudioScreen(onBack = upPress(navController))
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

private fun navigateToDetails(navController: NavHostController) = { taskId: String ->
    navController.navigate(
        TasksScreen.Details.route
            .plus("/$taskId")
    )
}
private fun upPress(navController: NavHostController): () -> Unit = { navController.navigateUp() }
