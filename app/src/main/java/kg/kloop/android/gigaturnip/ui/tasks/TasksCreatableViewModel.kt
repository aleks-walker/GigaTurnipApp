package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.data.responses.TaskResponseEntity
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
import timber.log.Timber
import javax.inject.Inject

data class TasksCreatableUiState(
    val taskStages: List<TaskStage> = emptyList(),
    val loading: Boolean = false,
    val creatingTask: Boolean = false,
    val createdTaskId: Int? = null,
    val taskStageId: String? = null,
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
    fun setCreatedTaskId(value: Int?) {
        _uiState.update { it.copy(createdTaskId = value) }
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


    fun createTask(stageId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _uiState.update { it.copy(creatingTask = true) }
                val token = getTokenSynchronously()
                val response = repository.createTask(token!!, stageId.toInt())
                val taskResponse = Gson().fromJson(
                    response.body()?.string(),
                    TaskResponseEntity::class.java
                )
                Timber.d("task response id: ${taskResponse.id}")
                _uiState.update {
                    it.copy(
                        taskStageId = stageId,
                        createdTaskId = taskResponse.id,
                        creatingTask = false
                    )
                }
            }
        }
    }

}