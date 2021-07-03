package kg.kloop.android.gigaturnip.domain

data class Task(
    val id: String,
    val responses: String,
    val stageId: Int,
    val caseId: Int,
    val inTasks: List<Int>?
)