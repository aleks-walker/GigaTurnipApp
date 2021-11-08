package kg.kloop.android.gigaturnip.data.models

import com.google.gson.annotations.SerializedName

data class PaginationDto<T>(
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: Int?,
    @SerializedName("previous")
    val previous: Int?,
    @SerializedName("results")
    val results: List<T>?
)