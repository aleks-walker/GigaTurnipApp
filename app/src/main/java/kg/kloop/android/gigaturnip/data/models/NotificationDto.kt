package kg.kloop.android.gigaturnip.data.models

import com.google.gson.annotations.SerializedName

data class NotificationDto (
    @SerializedName("title")
    val title: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("important")
    val important: Int
)