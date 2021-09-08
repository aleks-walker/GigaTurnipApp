package kg.kloop.android.gigaturnip.workers

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kg.kloop.android.gigaturnip.util.Constants.KEY_VIDEO_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

class CompressVideoWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_VIDEO_URI)
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            return coroutineScope {
                withContext(Dispatchers.IO) {
                    val notificationsHelper = NotificationsHelper(appContext)
                    val notificationId = 1

                    val outputData: Data?
                    val uri = compressFile(
                        context = appContext,
                        uri = Uri.parse(resourceUri),
                        onProgress = { progress ->
//                            setProgressAsync(workDataOf(PROGRESS to progress.toInt()))
                            notificationsHelper.updateNotificationProgress(
                                progress = progress.toInt(),
                                notificationId = notificationId
                            )
                        }
                    )
                    outputData = workDataOf(KEY_VIDEO_URI to uri.toString())
                    notificationsHelper.completeNotification(notificationId)
                    Timber.d("output data $outputData")
                    Result.success(outputData)
                }
            }
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error compressing video")
            return Result.failure()
        }

    }

}