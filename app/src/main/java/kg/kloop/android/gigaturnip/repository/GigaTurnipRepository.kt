package kg.kloop.android.gigaturnip.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.scopes.ActivityScoped
import kg.kloop.android.gigaturnip.data.models.mappers.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskStageDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.data.requests.TaskPostRequestEntity
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.util.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response

@ActivityScoped
class GigaTurnipRepository(
    private val api: GigaTurnipApi,
    private val campaignMapper: CampaignDtoMapper,
    private val tasksMapper: TaskDtoMapper,
    private val taskStageMapper: TaskStageDtoMapper
) {

    suspend fun getCampaignsList(token: String): Resource<List<Campaign>> {
        return getList({ api.getCampaignsList(token.toJwtToken()) }, campaignMapper)
    }

    suspend fun getCampaign(id: Int): Resource<Campaign> {
        return getSingle({ api.getCampaign(id) }, campaignMapper)

    }

    suspend fun getTasksList(
        token: String,
        complete: Boolean,
        campaignId: String
    ): Resource<List<Task>> {
        return getList({
            api.getTasksList(
                token = token.toJwtToken(),
                complete = complete,
                campaignId = campaignId
            )
        }, tasksMapper)

    }

    suspend fun getTaskById(
        token: String,
        id: Int?,
    ): Resource<Task> =
        getSingle({ api.getTaskById(token.toJwtToken(), id) }, tasksMapper)

    suspend fun getTasks(
        token: String,
        caseId: Int,
        stageId: Int
    ): Resource<List<Task>> =
        getList( { api.getTasks(token.toJwtToken(), caseId, stageId)}, tasksMapper)

    suspend fun getTaskStage(id: Int): Resource<TaskStage> {
        return getSingle({ api.getTaskStage(id) }, taskStageMapper)

    }

    suspend fun updateTask(
        token: String,
        id: Int,
        responses: String,
        complete: Boolean
    ): Response<ResponseBody> {
        val body = makeRequestBody(responses, complete)
        return api.updateTask(token.toJwtToken(), id, body)
    }

    private fun makeRequestBody(
        responses: String,
        complete: Boolean
    ): RequestBody = JsonObject().apply {
        addProperty("complete", complete)
        add("responses", JsonParser().parse(responses).asJsonObject)
    }.toString().toRequestBodyWithMediaType()

    suspend fun createTask(token: String, stageId: Int): Response<ResponseBody> {
        val json = Gson().toJson(TaskPostRequestEntity(stageId = stageId))
        return api.createTask(token.toJwtToken(), json.toRequestBodyWithMediaType())
    }

    suspend fun getTasksStagesList(
        token: String,
        isCreatable: Boolean,
        rankLimitsTotalLimit: Int,
        campaignId: String
    ): Resource<List<TaskStage>> {
        return getList({
            api.getTasksStagesList(
                token = token.toJwtToken(),
                isCreatable = isCreatable,
                rankLimitsTotalLimit = rankLimitsTotalLimit,
                campaignId = campaignId
            )
        }, taskStageMapper)

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

    private fun String.toRequestBodyWithMediaType() =
        this.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    private fun String.toJwtToken() = "JWT $this"
}