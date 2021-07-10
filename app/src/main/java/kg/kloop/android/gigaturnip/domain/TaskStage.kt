package kg.kloop.android.gigaturnip.domain

data class TaskStage(
    val id: String,
    val name: String,
    val description: String,
    val chain: Chain,
    val inStages: List<Int?>,
    val outStages: List<Int?>,
    val xPos: String,
    val yPos: String,
    val jsonSchema: String,
    val uiSchema: String,
    val library: String,
    val copyInput: Boolean,
    val allowMultipleFiles: Boolean,
    val isCreatable: Boolean,
    val displayedPrevStages: List<Int>,
    val assignUserBy: String,
    val assignUserFromStage: Int

)
