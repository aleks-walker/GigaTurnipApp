package kg.kloop.android.gigaturnip.data.responses

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage

data class TaskResponseEntity(
    val case: Int?,
    val complete: Boolean,
    val id: Int,
    @SerializedName("in_tasks")
    val inTasks: List<Int>?,
    val responses: JsonObject?,
    val stage: Int,
    val isReopened: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

fun TaskResponseEntity.toTask(stage: TaskStage) =
    Task(
    id = this.id.toString(),
    responses = this.responses,
    stage = stage,
    isComplete = this.complete,
    caseId = this.case,
    inTasks = this.inTasks,
    isReopened = this.isReopened,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)