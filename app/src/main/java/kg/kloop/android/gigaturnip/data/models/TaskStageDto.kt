package kg.kloop.android.gigaturnip.data.models

import com.google.gson.annotations.SerializedName

data class TaskStageDto (
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("json_schema")
    val jsonSchema: String,
    @SerializedName("ui_schema")
    val uiSchema: String,

    @SerializedName("library")
    val library: String,
    @SerializedName("x_pos")
    val xPos: String,
    @SerializedName("y_pos")
    val yPos: String,
    @SerializedName("copy_input")
    val copyInput: Boolean,
    @SerializedName("allow_multiple_files")
    val allowMultipleFiles: Boolean,
    @SerializedName("is_creatable")
    val isCreatable: Boolean,
    @SerializedName("count_complete")
    val countComplete: Boolean,
    @SerializedName("polymorphic_ctype")
    val polymorphicCType: Int,

    @SerializedName("chain")
    val chain: ChainDto,
    @SerializedName("in_stages")
    val inStages: List<Int>,
    @SerializedName("displayed_prev_stages")
    val displayedPrevStages: List<Int>,

)