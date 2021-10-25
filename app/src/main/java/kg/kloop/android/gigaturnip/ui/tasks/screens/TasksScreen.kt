package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.tasks.TasksViewModel
import kg.kloop.android.gigaturnip.ui.theme.ColorPalette


sealed class TasksScreen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object TasksList : TasksScreen("tasks_list", R.string.tasks_list, Icons.Filled.Home)
    object InProgress :
        TasksScreen("tasks_in_progress", R.string.in_progress, Icons.Filled.ArrowForward)

    object Finished : TasksScreen("tasks_finished", R.string.finished, Icons.Filled.Done)

    object Details : TasksScreen("task_details", R.string.details, Icons.Filled.ArrowDropDown)
    object Creatable : TasksScreen("task_creatable", R.string.creatable, Icons.Filled.ArrowDropDown)
}


@Composable
fun TasksScreenView(
    onFabClick: () -> Unit,
    navigateToDetails: (Task) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel,
) {
    val campaign = mainActivityViewModel.campaign.observeAsState()
    viewModel.setCampaignId(campaign.value!!.id)

    val uiState by viewModel.uiState.collectAsState()

    val navController = rememberNavController()
    val items = listOf(
        TasksScreen.InProgress,
        TasksScreen.Finished,
    )
    Scaffold(
        bottomBar = {
            TasksBottomNavigation(navController = navController, items = items)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFabClick() },
                shape = RoundedCornerShape(50),
                backgroundColor = ColorPalette.primary
            ) {
                Icon(Icons.Filled.Add, "")
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
    )
    { innerPadding ->

        NavHost(
            navController,
            startDestination = TasksScreen.InProgress.route,
            Modifier.padding(innerPadding)
        ) {
            composable(TasksScreen.InProgress.route) {
                TasksInProgress(
                    navigateToDetails,
                    uiState.inProgressTasks,
                    uiState.loading,
                    onRefresh = { viewModel.refreshTasks() }
                )
            }
            composable(TasksScreen.Finished.route) {
                TasksFinished(
                    navigateToDetails,
                    uiState.finishedTasks,
                    uiState.loading,
                    onRefresh = { viewModel.refreshTasks() }
                )
            }
        }
    }
}


@Composable
private fun TasksBottomNavigation(
    navController: NavHostController,
    items: List<TasksScreen>
) {
    return BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.resourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
            )
        }
    }
}
