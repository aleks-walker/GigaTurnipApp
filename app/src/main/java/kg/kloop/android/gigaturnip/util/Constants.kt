package kg.kloop.android.gigaturnip.util

object Constants {
    const val API_BASE_URL = "https://journal-bb5e3.uc.r.appspot.com/api/v1/"
//    const val API_BASE_URL = "http://10.0.2.2:8000/api/v1/"
    const val TURNIP_VIEW_URL = "https://kloopmedia.github.io/TurnipView/"
//    const val TURNIP_VIEW_URL = "http://10.0.2.2:3000"
    const val STORAGE_PUBLIC_PREFIX = "public/"
    const val STORAGE_PRIVATE_PREFIX = "private/"


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
    const val KEY_FILE_URI = "KEY_FILE_URI"
    const val KEY_PATH_TO_UPLOAD = "KEY_PATH_TO_UPLOAD"
    const val KEY_STORAGE_REF_PATH = "KEY_STORAGE_REF_PATH"
    const val KEY_FILENAME = "KEY_FILENAME"
    const val KEY_FILE_ID = "KEY_FILE_ID"

    // Progress Data Key
    const val PROGRESS = "PROGRESS"
    const val TAG_COMPRESS = "TAG_COMPRESS"
    const val TAG_UPLOAD = "TAG_UPLOAD"
    const val TAG_CLEANUP = "TAG_CLEANUP"

    // Webview events
    const val RICH_TEXT_EVENT = "android_rich_text_event"
    const val PREVIOUS_TASKS_EVENT = "android_previous_tasks_event"
    const val SCHEMA_EVENT = "android_schema_event"
    const val DATA_EVENT = "android_data_event"
    const val FILE_EVENT = "android_file_event"

    const val INPUT_DELAY_IN_MILL = 2000L
}