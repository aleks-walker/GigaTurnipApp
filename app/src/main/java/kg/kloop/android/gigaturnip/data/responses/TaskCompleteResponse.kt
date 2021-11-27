package kg.kloop.android.gigaturnip.data.responses

import com.google.gson.annotations.SerializedName

data class TaskCompleteResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("id")
    val currentTaskId: Int,

    @SerializedName("next_direct_id")
    val nextTaskId: Int?
)

