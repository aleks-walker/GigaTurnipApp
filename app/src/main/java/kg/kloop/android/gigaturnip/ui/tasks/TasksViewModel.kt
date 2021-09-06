package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Task
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

data class TasksUiState(
    val inProgressTasks: List<Task> = emptyList(),
    val finishedTasks: List<Task> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    private val _campaignId = MutableLiveData<String>()
    val campaignId: LiveData<String> = _campaignId
    fun setCampaignId(value: String) {
        _campaignId.value = value
    }

    private val _uiState = MutableStateFlow(TasksUiState(loading = true))
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        refreshTasks()
    }

    fun refreshTasks() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously()
                val inProgressTasks = repository.getTasksList(
                    token!!,
                    false,
                    _campaignId.value!!
                ).data.orEmpty()
                val finishedTasks = repository.getTasksList(
                    token,
                    true,
                    _campaignId.value!!
                ).data.orEmpty()
                _uiState.update {
                    it.copy(
                        inProgressTasks = inProgressTasks,
                        finishedTasks = finishedTasks,
                        loading = false
                    )
                }
            }
        }
    }

}