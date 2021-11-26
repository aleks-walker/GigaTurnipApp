package kg.kloop.android.gigaturnip.data.models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class TaskDto (

    @SerializedName("id")
    val id: String,

    @SerializedName("responses")
    val responses: JsonElement,

    @SerializedName("stage")
    val stage: TaskStageDto,

    @SerializedName("complete")
    val isComplete: Boolean,

    @SerializedName("case")
    val caseId: Int?,

    @SerializedName("in_tasks")
    val inTasks: List<Int>?,

    @SerializedName("reopened")
    val isReopened: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)