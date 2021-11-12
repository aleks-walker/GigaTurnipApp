package kg.kloop.android.gigaturnip.util

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun String.toTimeAgoFormat(): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .parse(this)!!.time
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        Calendar.getInstance().timeInMillis,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}