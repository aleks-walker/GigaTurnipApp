package kg.kloop.android.gigaturnip.data.models.mappers

import kg.kloop.android.gigaturnip.data.models.TaskStageDto
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.TaskStage

class TaskStageDtoMapper : DomainMapper<TaskStageDto, TaskStage>() {
    override fun mapToDomainModel(model: TaskStageDto): TaskStage {
        return TaskStage(
            id = model.id,
            name = model.name,
            description = model.description,
            jsonSchema = model.jsonSchema,
            uiSchema = model.uiSchema,

            library = model.library,
            xPos = model.xPos,
            yPos = model.yPos,
            copyInput = model.copyInput,
            allowMultipleFiles = model.allowMultipleFiles,
            isCreatable = model.isCreatable,
            countComplete = model.countComplete,
            polymorphicCType = model.polymorphicCType,

            chain = ChainDtoMapper().mapToDomainModel(model.chain),
            inStages = model.inStages,
            displayedPrevStages = model.displayedPrevStages,
        )
    }

    override fun mapFromDomainModel(domainModel: TaskStage): TaskStageDto {
        return TaskStageDto(
            id = domainModel.id,
            name = domainModel.name,
            description = domainModel.description,
            jsonSchema = domainModel.jsonSchema,
            uiSchema = domainModel.uiSchema,

            library = domainModel.library,
            xPos = domainModel.xPos,
            yPos = domainModel.yPos,
            copyInput = domainModel.copyInput,
            allowMultipleFiles = domainModel.allowMultipleFiles,
            isCreatable = domainModel.isCreatable,
            countComplete = domainModel.countComplete,
            polymorphicCType = domainModel.polymorphicCType,

            chain = ChainDtoMapper().mapFromDomainModel(domainModel.chain),
            inStages = domainModel.inStages,
            displayedPrevStages = domainModel.displayedPrevStages,
        )
    }
}