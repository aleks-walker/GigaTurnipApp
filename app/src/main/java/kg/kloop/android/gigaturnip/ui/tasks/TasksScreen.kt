package kg.kloop.android.gigaturnip.ui.tasks

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.theme.ColorPalette
import timber.log.Timber

private val TAG = TasksScreen::class.java.simpleName

sealed class TasksScreen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object TasksList : TasksScreen("tasks_list", R.string.tasks_list, Icons.Filled.Home)
    object InProgress :
        TasksScreen("tasks_in_progress", R.string.in_progress, Icons.Filled.ArrowForward)

    object Finished : TasksScreen("tasks_finished", R.string.finished, Icons.Filled.Done)
    object Details : TasksScreen("task_details", R.string.details, Icons.Filled.ArrowDropDown)
}


@Composable
fun TasksScreenView(
    viewModel: TasksViewModel = hiltViewModel()
) {
    val userId = "1"
    val navController = rememberNavController()
    val items = listOf(
        TasksScreen.InProgress,
        TasksScreen.Finished,
    )
    val fabShape = RoundedCornerShape(50)
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            TasksBottomNavigation(navController = navController, items = items)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { Toast.makeText(context, "Soon", Toast.LENGTH_SHORT).show() },
                shape = fabShape,
                backgroundColor = ColorPalette.primary
            ) {
                Icon(Icons.Filled.Add, "")
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
    )
    { innerPadding ->
        val tasksInProgress: List<Task> by viewModel.getTasksList(userId, false)
            .observeAsState(listOf())
        val tasksFinished: List<Task> by viewModel.getTasksList(userId, true).observeAsState(listOf())
        NavHost(
            navController,
            startDestination = TasksScreen.InProgress.route,
            Modifier.padding(innerPadding)
        ) {
            composable(TasksScreen.InProgress.route) {
                TasksInProgress(
                    navController,
                    tasksInProgress
                )
            }
            composable(TasksScreen.Finished.route) { TasksFinished(navController, tasksFinished) }
            composable(
                route = TasksScreen.Details.route.plus("/{id}/{stage_id}"),
                arguments = listOf(navArgument("id") { type = NavType.StringType },
                    navArgument("stage_id") { type = NavType.StringType })
            ) { TaskDetails(navController) }
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
        Timber.d("current destination: $currentDestination")
        Timber.d("details route: ${TasksScreen.Details.route}")
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
