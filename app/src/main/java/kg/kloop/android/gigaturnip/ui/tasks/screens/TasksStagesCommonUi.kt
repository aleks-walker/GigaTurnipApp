package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.kloop.android.gigaturnip.domain.TaskStage

@Composable
fun TaskStageList(
    taskStages: List<TaskStage>,
    isCreatingTask: Boolean,
    onClick: (TaskStage) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(taskStages) { stage ->
            TaskStageCard(
                stage,
                onClick = { onClick(stage) },
                isCreatingTask
            )
        }
    }
}

@Composable
private fun TaskStageCard(
    taskStage: TaskStage,
    onClick: () -> Unit,
    isCreatingTask: Boolean
) {
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
            if (isCreatingTask) {
                LinearProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp))
            }
            Text(text = taskStage.description, style = MaterialTheme.typography.body1)
        }
    }
}

