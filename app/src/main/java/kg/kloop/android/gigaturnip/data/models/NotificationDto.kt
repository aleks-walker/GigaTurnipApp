package kg.kloop.android.gigaturnip.data.models

import com.google.gson.annotations.SerializedName

data class NotificationDto (
    @SerializedName("id")
    val id: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("importance")
    val importance: Int,

    @SerializedName("campaign")
    val campaignId: Int,

    @SerializedName("rank")
    val rankId: Int
)