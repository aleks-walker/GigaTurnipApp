package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.compose.runtime.Composable
import kg.kloop.android.gigaturnip.domain.Task

@Composable
fun TasksFinished(
    onDetailsClick: (String) -> Unit,
    tasks: List<Task>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    TasksList(
        onDetailsClick = onDetailsClick,
        tasks = tasks,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    )
}
