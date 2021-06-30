package kg.kloop.android.gigaturnip.ui.tasks

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.models.Task

private val TAG = TasksScreen::class.java.simpleName

sealed class TasksScreen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object TasksList : TasksScreen("tasks_list", R.string.tasks_list, Icons.Filled.Home)
    object InProgress : TasksScreen("tasks_in_progress", R.string.in_progress, Icons.Filled.ArrowForward)
    object Finished : TasksScreen("tasks_finished", R.string.finished, Icons.Filled.Done)
    object Pending : TasksScreen("tasks_pending", R.string.pending, Icons.Filled.DateRange)
    object Details : TasksScreen("task_details", R.string.details, Icons.Filled.ArrowDropDown)
}


@Composable
fun TasksScreenView() {
    val navController = rememberNavController()
    val items = listOf(
        TasksScreen.InProgress,
        TasksScreen.Finished,
        TasksScreen.Pending
    )
    Scaffold(
        bottomBar = {
            TasksBottomNavigation(navController, items)
        }
    )
    { innerPadding ->
        val viewModel: TasksViewModel = viewModel()
        val tasks: List<Task> by viewModel.getTasks().observeAsState(listOf())
        NavHost(
            navController,
            startDestination = TasksScreen.InProgress.route,
            Modifier.padding(innerPadding)
        ) {
            composable(TasksScreen.InProgress.route) { TasksInProgress(navController, tasks) }
            composable(TasksScreen.Finished.route) { TasksFinished(navController) }
            composable(TasksScreen.Pending.route) { TasksPending(navController) }
            composable(
                route = TasksScreen.Details.route.plus("/{id}/{title}/{description}"),
                arguments = listOf(navArgument("id") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("description") { type = NavType.StringType })
            ) { TaskDetails(navController) }
        }
    }
}

@Composable
private fun TasksBottomNavigation(
    navController: NavHostController,
    items: List<TasksScreen>
) {
    BottomNavigation {
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
                }
            )
        }
    }
}


@Composable
fun TasksFinished(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Finished")

    }
}

@Composable
fun TasksPending(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Pending")

    }
}


