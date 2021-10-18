package kg.kloop.android.gigaturnip.data.models.mappers

import kg.kloop.android.gigaturnip.data.models.TaskStageDto
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.TaskStage

class TaskStageDtoMapper : DomainMapper<TaskStageDto, TaskStage>() {
    override fun mapToDomainModel(model: TaskStageDto): TaskStage {
        return TaskStage(
            id = model.id,
            name = model.name,
            description = model.description.toString(),
            chain = ChainDtoMapper().mapToDomainModel(model.chain),
            inStages = model.inStages,
            outStages = model.outStages,
            xPos = model.xPos,
            yPos = model.yPos,
            jsonSchema = model.jsonSchema.toString(),
            uiSchema = model.uiSchema.toString(),
            library = model.library,
            richText = model.richText,
            copyInput = model.copyInput,
            allowMultipleFiles = model.allowMultipleFiles,
            isCreatable = model.isCreatable,
            displayedPrevStages = model.displayedPrevStages,
            assignUserBy = model.assignUserBy,
            assignUserFromStage = model.assignUserFromStage
        )
    }

    override fun mapFromDomainModel(domainModel: TaskStage): TaskStageDto {
        return TaskStageDto(
            id = domainModel.id,
            name = domainModel.name,
            description = domainModel.description,
            chain = ChainDtoMapper().mapFromDomainModel(domainModel.chain),
            inStages = domainModel.inStages,
            outStages = domainModel.outStages,
            xPos = domainModel.xPos,
            yPos = domainModel.yPos,
            jsonSchema = domainModel.jsonSchema,
            uiSchema = domainModel.uiSchema,
            library = domainModel.library,
            richText = domainModel.richText,
            copyInput = domainModel.copyInput,
            allowMultipleFiles = domainModel.allowMultipleFiles,
            isCreatable = domainModel.isCreatable,
            displayedPrevStages = domainModel.displayedPrevStages,
            assignUserBy = domainModel.assignUserBy,
            assignUserFromStage = domainModel.assignUserFromStage
        )
    }
}