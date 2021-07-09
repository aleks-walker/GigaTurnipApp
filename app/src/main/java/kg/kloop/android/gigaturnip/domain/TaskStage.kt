package kg.kloop.android.gigaturnip.domain

data class TaskStage(
    val id: String,
    val name: String,
    val description: String,
    val jsonSchema: String,
    val uiSchema: String,

    val library: String,
    val xPos: String,
    val yPos: String,
    val copyInput: Boolean,
    val allowMultipleFiles: Boolean,
    val isCreatable: Boolean,
    val countComplete: Boolean,
    val polymorphicCType: Int,

    val chain: Int,
    val inStages: List<Int>,
    val displayedPrevStages: List<Int>,
)
