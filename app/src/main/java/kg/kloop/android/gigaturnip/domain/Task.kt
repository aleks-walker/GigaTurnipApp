package kg.kloop.android.gigaturnip.domain

import com.google.gson.JsonObject

data class Task(
    val id: String,
    val responses: JsonObject?,
    val stageId: Int,
    val caseId: Int,
    val inTasks: List<Int>?
)