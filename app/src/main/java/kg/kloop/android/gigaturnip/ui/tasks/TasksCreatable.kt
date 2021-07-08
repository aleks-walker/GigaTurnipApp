package kg.kloop.android.gigaturnip.ui.tasks

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.domain.Task


@Composable
fun TasksCreatable(navController: NavHostController, tasks: List<Task>) {
    TasksList(navController = navController, tasks = tasks)
}
