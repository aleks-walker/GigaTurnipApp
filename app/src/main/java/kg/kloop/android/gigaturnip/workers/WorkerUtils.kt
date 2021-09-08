package kg.kloop.android.gigaturnip.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.abedelazizshe.lightcompressorlibrary.CompressionProgressListener
import com.abedelazizshe.lightcompressorlibrary.Compressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.util.Constants.CHANNEL_ID
import kg.kloop.android.gigaturnip.util.Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import kg.kloop.android.gigaturnip.util.Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME
import timber.log.Timber
import java.io.File


class NotificationsHelper(val context: Context) {

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
            .setContentTitle(context.getString(R.string.video_compression))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun completeNotification(
        notificationId: Int
    ) {
        notificationBuilder.setContentText(context.getString(R.string.compression_complete))
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
    onProgress: (Float) -> Unit
): Uri? {
    val videoFile = File(uri.path!!)
    val downloadsPath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
    val desFile = File(downloadsPath, videoFile.name)
    desFile.createNewFile()

    val result = Compressor.compressVideo(
        context,
        srcUri = uri,
        srcPath = null,
        destination = desFile.path,
        listener = object : CompressionProgressListener {
            override fun onProgressChanged(percent: Float) {
                onProgress(percent)
            }

            override fun onProgressCancelled() {
            }
        },
        configuration = Configuration(
            quality = VideoQuality.MEDIUM,
            isMinBitRateEnabled = false,
            keepOriginalResolution = false,
        )
    )
    return if (result.success) {
        desFile.toUri()
    } else {
        Timber.d("result: $result")
        null
    }
}
