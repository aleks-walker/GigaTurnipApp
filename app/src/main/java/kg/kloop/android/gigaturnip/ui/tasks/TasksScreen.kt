package kg.kloop.android.gigaturnip.ui.tasks

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import kg.kloop.android.gigaturnip.R
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

private val TAG = TasksScreen::class.java.simpleName

sealed class TasksScreen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object TasksList : TasksScreen("tasks_list", R.string.tasks_list, Icons.Filled.Home)
    object InProgress :
        TasksScreen("tasks_in_progress", R.string.in_progress, Icons.Filled.ArrowForward)

    object Finished : TasksScreen("tasks_finished", R.string.finished, Icons.Filled.Done)
    object Pending : TasksScreen("tasks_pending", R.string.pending, Icons.Filled.DateRange)
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
        }
    }
}
private fun generateTasks(): List<Task> {
    return arrayListOf(
        Task("1", "First task", "Description"),
        Task("2", "Second task", "Description"),
        Task("3", "Third task", "Description")
    )
}


@Composable
fun TasksInProgress(navController: NavHostController, tasks: List<Task>) {
    Log.d(TAG, "TasksInProgress: $tasks")
    Column(modifier = Modifier.fillMaxSize()) {
        tasks.forEach { task ->
            TaskCard(task)
        }
    }
}

@Composable
private fun TaskCard(task: Task) {
    Card(
        modifier = Modifier.padding(4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            Text(text = task.id, style = MaterialTheme.typography.caption)
            Text(text = task.title, style = MaterialTheme.typography.h5)
            Text(text = task.description, style = MaterialTheme.typography.subtitle2)
        }
    }
//    Card(
//        modifier = Modifier
////                    .clickable(onClick = { navController.navigate(TasksScreen.TasksList.route) })
//            .fillMaxWidth()
//            .padding(16.dp),
//        elevation = 3.dp,
//        shape = MaterialTheme.shapes.medium
//    ) {
//        Text(
//            text = task.title,
//            modifier = Modifier.padding(16.dp),
//            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.body1
//        )
//    }
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

