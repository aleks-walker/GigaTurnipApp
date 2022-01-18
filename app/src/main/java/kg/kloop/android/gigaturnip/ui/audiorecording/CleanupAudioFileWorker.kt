package kg.kloop.android.gigaturnip.ui.audiorecording

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kg.kloop.android.gigaturnip.util.Constants
import timber.log.Timber
import java.io.File

class CleanupAudioFileWorker(ctx: Context, params: WorkerParameters): Worker(ctx, params) {
    override fun doWork(): Result {
        return try {
            val file = File(applicationContext.filesDir?.absolutePath, Constants.TEMP_AUDIO_FILE_NAME)
            if (file.exists()) {
                val deleted = file.delete()
                Timber.i("Deleted ${file.name} - $deleted")
            }
            Result.success()
        } catch (exception: Exception) {
            Timber.e(exception)
            Result.failure()
        }
    }
}