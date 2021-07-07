package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    fun getTasksList(
        userId: String,
        complete: Boolean
    ): LiveData<List<Task>> = liveData {
        emit(repository.getTasksList(userId, complete).data.orEmpty())
    }

}