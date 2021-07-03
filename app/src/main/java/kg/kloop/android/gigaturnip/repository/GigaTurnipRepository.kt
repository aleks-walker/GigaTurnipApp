package kg.kloop.android.gigaturnip.repository

import dagger.hilt.android.scopes.ActivityScoped
import kg.kloop.android.gigaturnip.data.models.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.models.TaskDtoMapper
import kg.kloop.android.gigaturnip.data.models.TaskStageDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.util.Resource

@ActivityScoped
class GigaTurnipRepository(
    private val api: GigaTurnipApi,
    private val campaignMapper: CampaignDtoMapper,
    private val tasksMapper: TaskDtoMapper,
    private val taskStageMapper: TaskStageDtoMapper
) {
    suspend fun getCampaignsList(): Resource<List<Campaign>> {
        return getList({ api.getCampaignsList() }, campaignMapper)
    }

    suspend fun getCampaign(id: Int): Resource<Campaign> {
        return getSingle({ api.getCampaign(id) }, campaignMapper)

    }

    suspend fun getTasksList(): Resource<List<Task>> {
        return getList({ api.getTasksList() }, tasksMapper)

    }

    suspend fun getTaskStage(id: Int): Resource<TaskStage> {
        return getSingle({ api.getTaskStage(id) }, taskStageMapper)

    }

    private suspend fun <T, DomainModel> getSingle(
        func: suspend () -> T,
        mapper: DomainMapper<T, DomainModel>
    ): Resource<DomainModel> {
        val response = try {
            func()
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error(e.message.toString())
        }
        return Resource.Success(mapper.mapToDomainModel(response))
    }

    private suspend fun <T, DomainModel> getList(
        func: suspend () -> List<T>,
        mapper: DomainMapper<T, DomainModel>
    ): Resource<List<DomainModel>> {
        val response = try {
            func()
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error(e.message.toString())
        }
        return Resource.Success(mapper.toDomainList(response))
    }
}