package kg.kloop.android.gigaturnip.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.scopes.ActivityScoped
import kg.kloop.android.gigaturnip.data.models.mappers.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.NotificationDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskStageDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.data.utils.getList
import kg.kloop.android.gigaturnip.data.utils.getSingle
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.util.Resource
import kg.kloop.android.gigaturnip.util.toJwtToken
import kg.kloop.android.gigaturnip.util.toRequestBodyWithMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*

@ActivityScoped
class GigaTurnipRepositoryImpl (
    private val api: GigaTurnipApi,
    private val campaignMapper: CampaignDtoMapper,
    private val tasksMapper: TaskDtoMapper,
    private val taskStageMapper: TaskStageDtoMapper,
    private val notificationDtoMapper: NotificationDtoMapper
): GigaTurnipRepository {


    override suspend fun getCampaignsList(token: String): Resource<List<Campaign>> =
        getList({ api.getCampaignsList(token.toJwtToken()) }, campaignMapper)

    override suspend fun getUserSelectableCampaignsList(token: String): Resource<List<Campaign>> =
        getList({ api.getUserSelectableCampaignsList(token.toJwtToken()) }, campaignMapper)

    override suspend fun getCampaign(id: Int): Resource<Campaign> =
        getSingle({ api.getCampaign(id) }, campaignMapper)

    override suspend fun joinCampaign(token: String, campaignId: String): Response<ResponseBody> =
        api.joinCampaign(token.toJwtToken(), campaignId)

    override suspend fun getTasksList(
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

    override suspend fun getTaskById(
        token: String,
        id: Int?,
    ): Resource<Task> =
        getSingle({ api.getTaskById(token.toJwtToken(), id) }, tasksMapper)

    override suspend fun openPreviousTask(
        token: String,
        taskId: Int,
    ): Response<ResponseBody> = api.openPreviousTask(token.toJwtToken(), taskId)

    override suspend fun getTasks(
        token: String,
        caseId: Int,
        stageId: Int
    ): Resource<List<Task>> =
        getList( { api.getTasks(token.toJwtToken(), caseId, stageId)}, tasksMapper)

    override suspend fun getPreviousTasks(
        token: String,
        taskId: Int
    ): Resource<List<Task>> =
        getList({ api.getPreviousTasksList(token.toJwtToken(), taskId) }, tasksMapper)

    override suspend fun getTaskStage(id: Int): Resource<TaskStage> {
        return getSingle({ api.getTaskStage(id) }, taskStageMapper)
    }

    override suspend fun updateTask(
        token: String,
        id: Int,
        responses: String,
        complete: Boolean
    ): Response<ResponseBody> {
        val body = makeRequestBody(responses, complete)
        return api.updateTask(token.toJwtToken(), id, body)
    }

    override fun makeRequestBody(
        responses: String,
        complete: Boolean
    ): RequestBody = JsonObject().apply {
        addProperty("complete", complete)
        add("responses", JsonParser().parse(responses).asJsonObject)
    }.toString().toRequestBodyWithMediaType()

    override suspend fun createTask(token: String, stageId: Int): Response<ResponseBody> =
        api.createTask(token.toJwtToken(), stageId)

    override suspend fun getTasksStagesList(
        token: String,
        campaignId: String
    ): Resource<List<TaskStage>> {
        return getList({
            api.getTasksStagesList(
                token = token.toJwtToken(),
                campaignId = campaignId
            )
        }, taskStageMapper)

    }

    override suspend fun getNotifications(
        token: String,
        campaignId: String,
        viewed: Boolean,
        importance: Int?,
    ): Resource<List<Notification>> = getList(
        {
            api.getNotifications(
                token = token.toJwtToken(),
                campaignId = campaignId,
                viewed = viewed,
                importance = importance
            ).results.orEmpty()
        },
        mapper = notificationDtoMapper
    )

    override suspend fun getNotification(
        token: String,
        notificationId: Int,
    ): Resource<Notification> = getSingle({
        api.getNotification(
            token.toJwtToken(),
            notificationId = notificationId
        )
    }, notificationDtoMapper)

    override fun getTokenSynchronously(onError: () -> Unit): String? {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            try {
                val result = Tasks.await(user.getIdToken(true))
                return Objects.requireNonNull(result).token
            } catch (e: Exception) {
                onError()
                e.printStackTrace()
            }
        }
        return null
    }

}