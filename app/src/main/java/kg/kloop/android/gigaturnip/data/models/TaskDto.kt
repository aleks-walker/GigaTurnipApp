package kg.kloop.android.gigaturnip.data.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TaskDto (

    @SerializedName("id")
    val id: String,

    @SerializedName("responses")
    val responses: JsonObject,

    @SerializedName("stage")
    val stageId: Int,

    @SerializedName("case")
    val caseId: Int,

    @SerializedName("in_tasks")
    val inTasks: List<Int>?,

)