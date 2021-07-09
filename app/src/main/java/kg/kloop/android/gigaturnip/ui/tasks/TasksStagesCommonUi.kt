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
import kg.kloop.android.gigaturnip.domain.TaskStage

@Composable
fun TaskStageList(onClick: (String) -> Unit, taskStages: List<TaskStage>) {
    Column(modifier = Modifier.fillMaxSize()) {
        taskStages.forEach { stage ->
            TaskStageCard(
                stage,
                onClick = {
                    onClick(stage.id)
//                    navController.navigate(
//                        TasksScreen.Details.route
//                            .plus("/${task.id}/${taskStage.id}")
//                    )
                })
        }
    }
}

@Composable
private fun TaskStageCard(taskStage: TaskStage, onClick: () -> Unit) {
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
            Text(text = taskStage.name, style = MaterialTheme.typography.h5)
            Text(text = taskStage.description, style = MaterialTheme.typography.body1)
        }
    }
}

