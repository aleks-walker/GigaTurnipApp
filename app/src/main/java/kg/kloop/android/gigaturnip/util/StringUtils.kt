package kg.kloop.android.gigaturnip.util

import android.annotation.SuppressLint
import android.text.format.DateUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

fun String.toRequestBodyWithMediaType() =
    this.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

fun String.toJwtToken() = "JWT $this"

fun String.encodeUrl() = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
