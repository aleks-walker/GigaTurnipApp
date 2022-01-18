package kg.kloop.android.gigaturnip.ui.audiorecording

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kg.kloop.android.gigaturnip.util.Constants
import kg.kloop.android.gigaturnip.util.encodeUrl
import kg.kloop.android.gigaturnip.workers.getFileName
import kotlinx.coroutines.coroutineScope

class UploadAudioFileWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val fileId = inputData.getString(Constants.AUDIO_FILE_KEY)
        val uri = inputData.getString(Constants.KEY_AUDIO_FILE_URI)!!.toUri()
        val fileName = uri.getFileName()
        val uploadPath = inputData.getString(Constants.KEY_PATH_TO_UPLOAD_AUDIO)

        val fileRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                uploadPath?.encodeUrl() + System.currentTimeMillis() + Constants.AUDIO_FILE_EXTENSION
            )
        val uploadTask = fileRef.putFile(uri)

        return coroutineScope {
            Tasks.await(uploadTask.addOnSuccessListener {

            }.addOnFailureListener{

            })
            Result.success(
                workDataOf(
                    Constants.KEY_AUDIO_FILE_ID to fileId,
                    Constants.KEY_AUDIO_FILENAME to fileName,
                    Constants.KEY_PATH_TO_UPLOAD_AUDIO to fileRef.path
                )
            )
        }
    }
}