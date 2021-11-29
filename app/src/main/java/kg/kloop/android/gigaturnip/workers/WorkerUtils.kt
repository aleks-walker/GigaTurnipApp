package kg.kloop.android.gigaturnip.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.abedelazizshe.lightcompressorlibrary.CompressionProgressListener
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.compressor.Compressor
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.util.Constants.CHANNEL_ID
import kg.kloop.android.gigaturnip.util.Constants.OUTPUT_PATH
import kg.kloop.android.gigaturnip.util.Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import kg.kloop.android.gigaturnip.util.Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME
import timber.log.Timber
import java.io.File


class NotificationsHelper(private val context: Context, val title: String) {

    private val notificationBuilder: NotificationCompat.Builder

    init {
        createNotificationChannel(context)
        notificationBuilder = getNotificationBuilder()
    }

    private fun createNotificationChannel(context: Context) {
        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val channel = NotificationChannel(
                CHANNEL_ID,
                VERBOSE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun completeNotification(
        notificationId: Int,
        text: String
    ) {
        notificationBuilder.setContentText(text)
            .setProgress(0, 0, false)
        NotificationManagerCompat.from(context).apply {
            notify(notificationId, notificationBuilder.build())
        }
    }

    fun updateNotificationProgress(
        progress: Int,
        notificationId: Int,
        maxProgress: Int = 100
    ) {
        NotificationManagerCompat.from(context).apply {
            notificationBuilder.apply {
                setProgress(maxProgress, progress, false)
                setContentText("${progress}%")
            }
            notify(notificationId, notificationBuilder.build())
        }
    }

}

@WorkerThread
fun compressFile(
    context: Context,
    uri: Uri,
    onProgress: (Float) -> Unit,
    onCancel: () -> Unit
): Uri? {
    val videoFile = File(uri.path!!)
    val outputDir = File(context.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) { outputDir.mkdirs() }
    val desFile = File(outputDir, videoFile.name)
    desFile.createNewFile()

    Compressor.isRunning = true
    val result = Compressor.compressVideo(
        context,
        srcUri = uri,
        srcPath = null,
        destination = desFile.path,
        streamableFile = null,
        listener = object : CompressionProgressListener {
            override fun onProgressChanged(percent: Float) {
                Timber.d("Compress progress: ${percent.toInt()}")
                onProgress(percent)
            }

            override fun onProgressCancelled() {
                Timber.d("Compression cancelled")
                onCancel()
            }
        },
        configuration = Configuration(
            quality = VideoQuality.MEDIUM,
        )
    )
    return if (result.success) {
        desFile.toUri()
    } else {
        Timber.d("result: $result")
        null
    }
}
