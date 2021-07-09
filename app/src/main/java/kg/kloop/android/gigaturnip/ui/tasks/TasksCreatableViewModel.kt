package kg.kloop.android.gigaturnip.ui.tasks

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

    fun getTasksStagesList(token: String, isCreatable: Boolean): LiveData<List<TaskStage>> =
        liveData {
            emit(repository.getTasksStagesList(token, isCreatable, 0).data.orEmpty())
        }

    private val _taskResponse = MutableLiveData<TaskResponseEntity>()
    val taskResponseEntity: LiveData<TaskResponseEntity> = _taskResponse

    fun createTask(token: String, stageId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val response = repository.createTask(token, stageId.toInt())
                val taskResponse = Gson().fromJson(response.body()?.string(), TaskResponseEntity::class.java)
                Timber.d("task response id: ${taskResponse.id}")
                _taskResponse.postValue(taskResponse)
            }

        }
    }

}