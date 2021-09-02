package kg.kloop.android.gigaturnip.ui.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.data.responses.TaskResponseEntity
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TasksCreatableViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    fun getTasksStagesList(
        token: String,
        isCreatable: Boolean,
        campaignId: String
    ): LiveData<List<TaskStage>> =
        liveData {
            emit(
                repository.getTasksStagesList(
                    token,
                    isCreatable, 0, campaignId
                ).data.orEmpty()
            )
        }

    private val _taskResponse = MutableLiveData<TaskResponseEntity>()
    val taskResponseEntity: LiveData<TaskResponseEntity> = _taskResponse

    private val _taskStageId = MutableLiveData<Int?>()
    val taskStageId: LiveData<Int?> = _taskStageId

    fun setTaskStageId(value: Int?) {
        _taskStageId.postValue(value)
    }

    val loading = mutableStateOf(false)

    fun createTask(token: String, stageId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                loading.value = true
                val response = repository.createTask(token, stageId.toInt())
                val taskResponse = Gson().fromJson(response.body()?.string(), TaskResponseEntity::class.java)
                Timber.d("task response id: ${taskResponse.id}")
                if (_taskResponse.value == null) _taskResponse.postValue(taskResponse)
                loading.value = false
            }

        }
    }

}