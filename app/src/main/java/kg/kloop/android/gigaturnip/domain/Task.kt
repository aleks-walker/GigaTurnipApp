package kg.kloop.android.gigaturnip.domain

import com.google.gson.JsonObject

data class Task(
    val id: String,
    val responses: JsonObject?,
    val stage: TaskStage,
    val caseId: Int,
    val inTasks: List<Int>?
)