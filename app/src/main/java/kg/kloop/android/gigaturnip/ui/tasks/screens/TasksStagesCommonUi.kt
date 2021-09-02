package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.ui.tasks.TasksCreatableViewModel

@Composable
fun TaskStageList(onClick: (String) -> Unit,
                  taskStages: List<TaskStage>,
                  viewModel: TasksCreatableViewModel = hiltViewModel(),
) {
    val loading = viewModel.loading.value
    Column(modifier = Modifier.fillMaxSize()) {
        taskStages.forEach { stage ->
            TaskStageCard(
                stage,
                onClick = {
                    if (!loading){
                        onClick(stage.id)
                        viewModel.setTaskStageId(stage.id.toInt())
                    }
                },
                loading
            )
        }
    }
}

@Composable
private fun TaskStageCard(
    taskStage: TaskStage,
    onClick: () -> Unit,
    loading: Boolean
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
            if (loading) {
                LinearProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp))
            }
            Text(text = taskStage.description, style = MaterialTheme.typography.body1)
        }
    }
}

