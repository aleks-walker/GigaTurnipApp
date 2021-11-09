package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.data.responses.TaskResponseEntity
import kg.kloop.android.gigaturnip.data.responses.toTask
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class TasksCreatableUiState(
    val taskStages: List<TaskStage> = emptyList(),
    val loading: Boolean = false,
    val creatingTask: Boolean = false,
    val createdTask: Task? = null,
    val taskStage: TaskStage? = null,
) {
    val initialLoad: Boolean
        get() = taskStages.isEmpty() && loading
}

@HiltViewModel
class TasksCreatableViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val campaignId: String = savedStateHandle.get<String>("campaign_id")!!

    private val _uiState = MutableStateFlow(TasksCreatableUiState(loading = true))
    val uiState: StateFlow<TasksCreatableUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }
    fun setCreatedTask(value: Task?) {
        _uiState.update { it.copy(createdTask = value) }
    }

    fun refreshAll() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously()
                val result = repository.getTasksStagesList(
                    token!!,
                    campaignId
                ).data.orEmpty()
                _uiState.update { it.copy(taskStages = result, loading = false) }
            }
        }
    }


    fun createTask(stage: TaskStage) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _uiState.update { it.copy(creatingTask = true) }
                val token = getTokenSynchronously()
                val response = repository.createTask(token!!, stage.id.toInt())
                val taskResponse = Gson().fromJson(
                    response.body()?.string(),
                    TaskResponseEntity::class.java
                )
                _uiState.update {
                    it.copy(
                        taskStage = stage,
                        createdTask = taskResponse.toTask(stage),
                        creatingTask = false
                    )
                }
            }
        }
    }

}