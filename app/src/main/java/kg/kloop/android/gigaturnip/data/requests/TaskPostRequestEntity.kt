package kg.kloop.android.gigaturnip.data.requests

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TaskPostRequestEntity(
    val responses: JsonObject? = JsonObject(),
    @SerializedName("complete")
    val isComplete: Boolean = false,
    @SerializedName("stage")
    val stageId: Int
)