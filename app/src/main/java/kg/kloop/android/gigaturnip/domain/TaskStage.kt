package kg.kloop.android.gigaturnip.domain

import com.google.gson.JsonObject

data class TaskStage(
    val id: String,
    val name: String,
    val description: String,
    val jsonSchema: JsonObject,
    val uiSchema: JsonObject,

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
