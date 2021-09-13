package kg.kloop.android.gigaturnip.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kg.kloop.android.gigaturnip.util.Constants.OUTPUT_PATH
import timber.log.Timber
import java.io.File

class CleanupWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty()) {
                            val deleted = entry.delete()
                            Timber.i("Deleted $name - $deleted")
                        }
                    }
                }
            }
            Result.success()
        } catch (exception: Exception) {
            Timber.e(exception)
            Result.failure()
        }
    }
}


