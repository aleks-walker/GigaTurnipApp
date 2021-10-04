package kg.kloop.android.gigaturnip.domain

data class Campaign(
    val id: String,
    val title: String,
    val description: String = "",
    val defaultTrack: String?,
    val managers: List<Int>
)