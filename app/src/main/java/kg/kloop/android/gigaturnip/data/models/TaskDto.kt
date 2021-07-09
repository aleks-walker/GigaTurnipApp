package kg.kloop.android.gigaturnip.data.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TaskDto (

    @SerializedName("id")
    val id: String,

    @SerializedName("responses")
    val responses: JsonObject?,

    @SerializedName("stage")
    val stage: TaskStageDto,

    @SerializedName("complete")
    val isComplete: Boolean,

    @SerializedName("case")
    val caseId: Int?,

    @SerializedName("in_tasks")
    val inTasks: List<Int>?,

)