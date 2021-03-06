package kg.kloop.android.gigaturnip.workers

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.work.*
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILENAME
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_ID
import kg.kloop.android.gigaturnip.util.Constants.KEY_FILE_URI
import kg.kloop.android.gigaturnip.util.Constants.KEY_PATH_TO_UPLOAD
import kg.kloop.android.gigaturnip.util.Constants.PROGRESS
import kg.kloop.android.gigaturnip.util.Constants.TAG_COMPRESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class CompressVideoWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val fileId = inputData.getString(KEY_FILE_ID)
        val resourceUri = inputData.getString(KEY_FILE_URI)
        val pathToUpload = inputData.getString(KEY_PATH_TO_UPLOAD)

        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            return coroutineScope {
                withContext(Dispatchers.IO) {
                    val notificationsHelper = NotificationsHelper(
                        appContext,
                        appContext.getString(R.string.video_compression)
                    )
                    val notificationId = 1

                    val outputData: Data?
                    val uri = compressFile(
                        context = appContext,
                        uri = Uri.parse(resourceUri),
                        onProgress = { progress ->
                            setProgressAsync(workDataOf(
                                KEY_FILE_ID to fileId,
                                KEY_FILENAME to resourceUri!!.toUri().getFileName(),
                                KEY_PATH_TO_UPLOAD to pathToUpload,
                                PROGRESS to progress.toInt(),
                            ))
                            notificationsHelper.updateNotificationProgress(
                                progress = progress.toInt(),
                                notificationId = notificationId
                            )
                        },
                        onCancel = {
                            notificationsHelper.completeNotification(
                                notificationId,
                                appContext.getString(R.string.cancelled)
                            )
                            cancelWork(appContext)
                        }
                    )
                    outputData = workDataOf(
                        KEY_FILE_ID to fileId,
                        KEY_FILENAME to uri!!.getFileName(),
                        KEY_PATH_TO_UPLOAD to pathToUpload,
                        KEY_FILE_URI to uri.toString(),
                    )
                    notificationsHelper.completeNotification(
                        notificationId,
                        appContext.getString(R.string.compression_complete)
                    )
                    Timber.d("output data $outputData")
                    Result.success(outputData)
                }
            }
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error compressing video")
            return Result.failure()
        }

    }

    private fun cancelWork(appContext: Context) {
        WorkManager.getInstance(appContext).cancelAllWorkByTag(TAG_COMPRESS)
    }

}

fun Uri.getFileName(): String = File(this.path!!).name
