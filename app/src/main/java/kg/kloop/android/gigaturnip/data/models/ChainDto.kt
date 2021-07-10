package kg.kloop.android.gigaturnip.data.models

import com.google.gson.annotations.SerializedName

data class ChainDto(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("campaign")
    val campaignId: Int
)