package kg.kloop.android.gigaturnip.ui.audiorecording

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.util.Constants
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_EXTENSION
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_FILE_KEY
import kg.kloop.android.gigaturnip.util.Constants.AUDIO_PROGRESS
import kg.kloop.android.gigaturnip.util.Constants.KEY_AUDIO_FILENAME
import kg.kloop.android.gigaturnip.util.Constants.KEY_AUDIO_FILE_ID
import kg.kloop.android.gigaturnip.util.Constants.KEY_AUDIO_FILE_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_AUDIO_STORAGE_REF_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_PATH_TO_UPLOAD_AUDIO
import kg.kloop.android.gigaturnip.util.encodeUrl
import kg.kloop.android.gigaturnip.workers.NotificationsHelper
import kg.kloop.android.gigaturnip.workers.getFileName
import kotlinx.coroutines.coroutineScope

class UploadAudioFileWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val fileId = inputData.getString(AUDIO_FILE_KEY)
        val uri = inputData.getString(KEY_AUDIO_FILE_URI)!!.toUri()
        val fileName = uri.getFileName()
        val uploadPath = inputData.getString(KEY_PATH_TO_UPLOAD_AUDIO)

        val fileRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                uploadPath?.encodeUrl() + System.currentTimeMillis() + AUDIO_FILE_EXTENSION
            )
        val uploadTask = fileRef.putFile(uri)
        val notificationsHelper = NotificationsHelper(
            applicationContext,
            context.getString(R.string.uploading_file)
        )
        val notificationId = 1

        return coroutineScope {
            Tasks.await(uploadTask.addOnSuccessListener {
                val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                setProgressAsync(
                    workDataOf(
                        KEY_AUDIO_FILE_ID to fileId,
                        KEY_AUDIO_FILENAME to fileName,
                        KEY_PATH_TO_UPLOAD_AUDIO to uploadPath,
                        AUDIO_PROGRESS to progress.toInt(),
                    )
                )
                notificationsHelper.updateNotificationProgress(
                    progress.toInt(),
                    notificationId,
                    100
                )
            }.addOnFailureListener{
                notificationsHelper.completeNotification(
                    notificationId, it.localizedMessage!!.toString()
                )
            })
            notificationsHelper.completeNotification(
                notificationId,
                context.getString(R.string.file_uploaded)
            )
            Result.success(
                workDataOf(
                    KEY_AUDIO_FILE_ID to fileId,
                    KEY_AUDIO_FILENAME to fileName,
                    KEY_AUDIO_STORAGE_REF_PATH to fileRef.path
                )
            )
        }
    }
}