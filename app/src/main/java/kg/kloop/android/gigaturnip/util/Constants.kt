package kg.kloop.android.gigaturnip.util

object Constants {
    const val API_BASE_URL = "https://journal-bb5e3.uc.r.appspot.com/api/v1/"
//    const val API_BASE_URL = "http://10.0.2.2:8000/api/v1/"
    const val TURNIP_VIEW_URL = "https://kloopmedia.github.io/TurnipView/"
//    const val TURNIP_VIEW_URL = "http://10.0.2.2:3000"
    const val STORAGE_BASE_URL = "https://firebasestorage.googleapis.com"


    // Notification Channel constants

    // Name of Notification Channel for verbose notifications of background work
    @JvmField
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"

    // The name of the video manipulation work
    const val VIDEO_MANIPULATION_WORK_NAME = "video_manipulation_work"

    // Other keys
    const val OUTPUT_PATH = "compress_video_outputs"
    const val KEY_VIDEO_URI = "KEY_VIDEO_URI"
    const val KEY_UPLOAD_PATH = "KEY_UPLOAD_PATH"
    const val KEY_FILE_PATH = "KEY_FILE_PATH"
    const val KEY_DOWNLOAD_URI = "KEY_DOWNLOAD_URI"
    const val KEY_FILENAME = "KEY_FILENAME"

    // Progress Data Key
    const val PROGRESS = "PROGRESS"
    const val TAG_COMPRESS = "TAG_COMPRESS"
    const val TAG_UPLOAD = "TAG_UPLOAD"
    const val TAG_CLEANUP = "TAG_CLEANUP"


}