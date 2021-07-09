package kg.kloop.android.gigaturnip.ui.tasks

import androidx.compose.runtime.Composable
import kg.kloop.android.gigaturnip.domain.Task

@Composable
fun TasksFinished(onDetailsClick: (Task) -> Unit, tasks: List<Task>) {
    TasksList(onDetailsClick = onDetailsClick, tasks = tasks)
}
