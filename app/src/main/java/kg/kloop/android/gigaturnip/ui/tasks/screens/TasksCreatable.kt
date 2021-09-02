package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.ui.tasks.TasksCreatableViewModel


@Composable
fun TasksCreatable(
    navController: NavHostController,
    mainActivityViewModel: MainActivityViewModel,
    viewModel: TasksCreatableViewModel = hiltViewModel()
) {
    val token = mainActivityViewModel.getUserToken().observeAsState()
    val campaignId = mainActivityViewModel.campaignId.observeAsState()

    val creatableTasksStages: List<TaskStage> by viewModel.getTasksStagesList(
        token.value.toString(),
        true,
        campaignId.value.toString()
    ).observeAsState(listOf())

    val taskResponse by viewModel.taskResponseEntity.observeAsState()
    val taskStageId: Int? by viewModel.taskStageId.observeAsState(null)

    TaskStageList(
        onClick = { stageId ->
            if (taskResponse == null) {
                viewModel.createTask(token.value.toString(), stageId)
            }
        },
        taskStages = creatableTasksStages
    )

    if (taskResponse != null && taskStageId != null) {
        navController.popBackStack()
        navController.navigate(
            TasksScreen.Details.route.plus("/${taskResponse!!.id}/$taskStageId")
        )
    }
}
