package kg.kloop.android.gigaturnip.workers

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.util.Constants.KEY_DOWNLOAD_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_UPLOAD_PATH
import kg.kloop.android.gigaturnip.util.Constants.KEY_VIDEO_URI
import java.io.File

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

        var downloadUri = ""
        uploadTask.addOnProgressListener { //(bytesTransferred, totalByteCount) ->
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            notificationsHelper.updateNotificationProgress(progress.toInt(), notificationId, 100)
        }.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener {
                notificationsHelper.completeNotification(
                    notificationId,
                    context.getString(R.string.file_uploaded)
                )
                downloadUri = it.toString()
            }
        }.addOnFailureListener {
            notificationsHelper.completeNotification(
                notificationId, it.localizedMessage!!.toString()
            )
        }
        return Result.success(workDataOf(KEY_DOWNLOAD_URI to downloadUri))
    }

    private fun getStorageRef(
        uploadPath: String,
        fileName: String
    ) = Firebase.storage.reference.child(uploadPath.plus("/$fileName.mp4"))

    private fun getFileName(uri: Uri) = File(uri.path!!).name

}