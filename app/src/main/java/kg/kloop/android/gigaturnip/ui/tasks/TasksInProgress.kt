package kg.kloop.android.gigaturnip.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.models.Task

@Composable
fun TasksInProgress(navController: NavHostController, tasks: List<Task>) {
    Column(modifier = Modifier.fillMaxSize()) {
        tasks.forEach { task ->
            TaskCard(
                task,
                onClick = {
                    navController.navigate(
                        TasksScreen.Details.route
                            .plus("/${task.id}/${task.title}/${task.description}")
                    )
                })
        }
    }
}

@Composable
private fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clickable(onClick = onClick),
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
}
