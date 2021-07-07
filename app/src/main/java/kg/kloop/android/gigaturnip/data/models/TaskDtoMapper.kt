package kg.kloop.android.gigaturnip.data.models

import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Task

class TaskDtoMapper : DomainMapper<TaskDto, Task>(){
    override fun mapToDomainModel(model: TaskDto): Task {
        return Task(
            id = model.id,
            responses = model.responses,
            stage = TaskStageDtoMapper().mapToDomainModel(model.stage),
            caseId = model.caseId,
            inTasks = model.inTasks
        )
    }

    override fun mapFromDomainModel(domainModel: Task): TaskDto {
        return TaskDto(
            id = domainModel.id,
            responses = domainModel.responses,
            stage = TaskStageDtoMapper().mapFromDomainModel(domainModel.stage),
            caseId = domainModel.caseId,
            inTasks = domainModel.inTasks
        )
    }
}