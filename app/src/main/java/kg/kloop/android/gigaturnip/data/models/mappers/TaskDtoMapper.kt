package kg.kloop.android.gigaturnip.data.models.mappers

import kg.kloop.android.gigaturnip.data.models.TaskDto
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Task

class TaskDtoMapper : DomainMapper<TaskDto, Task>(){
    override fun mapToDomainModel(model: TaskDto): Task {
        return Task(
            id = model.id,
            responses = model.responses,
            stage = TaskStageDtoMapper().mapToDomainModel(model.stage),
            isComplete = model.isComplete,
            caseId = model.caseId,
            inTasks = model.inTasks
        )
    }

    override fun mapFromDomainModel(domainModel: Task): TaskDto {
        return TaskDto(
            id = domainModel.id,
            responses = domainModel.responses,
            stage = TaskStageDtoMapper().mapFromDomainModel(domainModel.stage),
            isComplete = domainModel.isComplete,
            caseId = domainModel.caseId,
            inTasks = domainModel.inTasks
        )
    }
}