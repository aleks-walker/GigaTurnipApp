package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TasksViewModel : ViewModel() {
    private val tasks: MutableLiveData<List<Task>> = MutableLiveData<List<Task>>(generateTasks())

    fun getTasks(): LiveData<List<Task>> {
        return tasks
    }

    private fun generateTasks(): List<Task> {
        return arrayListOf(
            Task("1", "First task", "Description"),
            Task("2", "Second task", "Description"),
            Task("3", "Third task", "Description")
        )
    }

}