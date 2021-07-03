package kg.kloop.android.gigaturnip.data.models

import com.google.gson.JsonObject
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Task

class TaskDtoMapper : DomainMapper<TaskDto, Task>(){
    override fun mapToDomainModel(model: TaskDto): Task {
        return Task(
            id = model.id,
            responses = model.responses.toString(),
            stageId = model.stageId,
            caseId = model.caseId,
            inTasks = model.inTasks
        )
    }

    override fun mapFromDomainModel(domainModel: Task): TaskDto {
        return TaskDto(
            id = domainModel.id,
            responses = JsonObject().getAsJsonObject(domainModel.responses),
            stageId = domainModel.stageId,
            caseId = domainModel.caseId,
            inTasks = domainModel.inTasks
        )
    }
}