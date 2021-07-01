package kg.kloop.android.gigaturnip.domain

data class Task(
    val id: String,
    val title: String,
    val description: String = ""
)