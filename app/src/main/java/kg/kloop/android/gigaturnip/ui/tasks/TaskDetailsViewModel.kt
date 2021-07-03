package kg.kloop.android.gigaturnip.ui.tasks

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.iceteck.silicompressorr.SiliCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    private val fileCompressor: SiliCompressor
) : ViewModel() {

    fun getTaskStage(id: Int): LiveData<TaskStage> = liveData {
        repository.getTaskStage(id).data?.let { emit(it) }
    }


    fun getCompressedFilePath(uri: Uri, destination: String): LiveData<String> = liveData {
        fileCompressor.compressVideo(uri, destination)
    }


}