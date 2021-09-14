package kg.kloop.android.gigaturnip.workers

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.util.Constants.KEY_DOWNLOAD_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_VIDEO_URI
import kg.kloop.android.gigaturnip.util.Constants.PROGRESS
import kg.kloop.android.gigaturnip.util.getFileName
import kotlinx.coroutines.coroutineScope

class UploadFileWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val uri = inputData.getString(KEY_VIDEO_URI)!!.toUri()
        val uploadPath = inputData.getString(KEY_UPLOAD_PATH)!!

        val fileName = getFileName(uri)
        val fileRef = getStorageRef(uploadPath, fileName)
        val uploadTask = fileRef.putFile(uri)
        val notificationsHelper = NotificationsHelper(
            applicationContext,
            context.getString(R.string.uploading_file)
        )
        val notificationId = 1

        return coroutineScope {
            Tasks.await(uploadTask.addOnProgressListener { //(bytesTransferred, totalByteCount) ->
                val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                setProgressAsync(workDataOf(PROGRESS to progress.toInt()))
                notificationsHelper.updateNotificationProgress(
                    progress.toInt(),
                    notificationId,
                    100
                )
            }.addOnFailureListener {
                notificationsHelper.completeNotification(
                    notificationId, it.localizedMessage!!.toString()
                )
            })
            val downloadUri = Tasks.await(fileRef.downloadUrl)
            notificationsHelper.completeNotification(
                notificationId,
                context.getString(R.string.file_uploaded)
            )
            Result.success(
                workDataOf(
                    KEY_DOWNLOAD_URI to downloadUri.toString(),
                    KEY_FILE_PATH to fileRef.path
                )
            )

        }
    }

    private fun getStorageRef(
        uploadPath: String,
        fileName: String
    ) = Firebase.storage.reference.child(uploadPath.plus("/$fileName.mp4"))


}