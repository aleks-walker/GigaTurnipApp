package kg.kloop.android.gigaturnip.data.models

import com.google.gson.annotations.SerializedName

data class CampaignDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("default_track")
    val defaultTrack: String?,

    @SerializedName("managers")
    val managers: List<Int>
)