package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.DetailsToolbar
import kg.kloop.android.gigaturnip.ui.components.FullScreenLoading
import kg.kloop.android.gigaturnip.ui.tasks.TasksCreatableViewModel


@Composable
fun TasksCreatable(
    navController: NavHostController,
    viewModel: TasksCreatableViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navigateToTask: (Task) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.createdTask != null && !uiState.creatingTask) {
        navController.popBackStack()
        navigateToTask(uiState.createdTask!!)
        viewModel.setCreatedTask(null)
    } else {
        Scaffold (
            topBar = {
                DetailsToolbar(
                    title = stringResource(id = R.string.create_form),
                    onBack = onBack,
                )
            },
        ) {
            LoadingContent(empty = uiState.initialLoad,
                emptyContent = { FullScreenLoading() },
                loading = uiState.loading,
                onRefresh = { viewModel.refreshAll() }) {
                TaskStageList(
                    taskStages = uiState.taskStages,
                    isCreatingTask = uiState.creatingTask,
                    onClick = { stage -> viewModel.createTask(stage) },
                )
            }
        }
    }

}

@Composable
private fun LoadingContent(
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    loading: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
            onRefresh = onRefresh,
            content = content,
        )
    }
}

