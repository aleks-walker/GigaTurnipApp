package kg.kloop.android.gigaturnip.data.responses

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TaskResponseEntity(
    val case: Int?,
    val complete: Boolean,
    val id: Int,
    @SerializedName("in_tasks")
    val inTasks: List<Int>?,
    val responses: JsonObject?,
    val stage: Int
)