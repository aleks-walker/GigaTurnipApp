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
    val stage: Int
)

fun TaskResponseEntity.toTask(stage: TaskStage) =
    Task(
    this.id.toString(),
    this.responses,
    stage,
    this.complete,
    this.case,
    this.inTasks
)