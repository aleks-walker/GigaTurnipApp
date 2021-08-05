package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: MutableLiveData<Boolean> = _isRefreshing
    fun setIsRefreshing(value: Boolean) {
        _isRefreshing.value = value
    }

    fun getTasksList(
        token: String,
        complete: Boolean
    ): LiveData<List<Task>> = liveData {
//        _isRefreshing.postValue(true)
        Timber.d("Get tasks list")
        emit(repository.getTasksList(token, complete).data.orEmpty())
        _isRefreshing.postValue(false)
    }

}