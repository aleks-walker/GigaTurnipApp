package kg.kloop.android.gigaturnip.domain

data class Notification(
    val id: Int,
    val title: String,
    val text: String,
    val importance: Int,
    val createdAt: String,
    val updatedAt: String,
    val campaignId: Int,
    val rankId: Int
)