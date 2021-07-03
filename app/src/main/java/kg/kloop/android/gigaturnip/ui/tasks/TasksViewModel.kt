package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    val tasks = liveData {
        emit(repository.getTasksList().data.orEmpty())
    }

}