package kg.kloop.android.gigaturnip.repository

import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.util.Resource
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

interface GigaTurnipRepository {

    suspend fun getCampaignsList(token: String): Resource<List<Campaign>>

    suspend fun getUserSelectableCampaignsList(token: String): Resource<List<Campaign>>

    suspend fun getCampaign(id: Int): Resource<Campaign>

    suspend fun joinCampaign(token: String, campaignId: String): Response<ResponseBody>

    suspend fun getTasksList(
        token: String,
        complete: Boolean,
        campaignId: String
    ): Resource<List<Task>>

    suspend fun getTaskById(
        token: String,
        id: Int?,
    ): Resource<Task>

    suspend fun openPreviousTask(
        token: String,
        taskId: Int,
    ): Response<ResponseBody>

    suspend fun getTasks(
        token: String,
        caseId: Int,
        stageId: Int
    ): Resource<List<Task>>

    suspend fun getPreviousTasks(
        token: String,
        taskId: Int
    ): Resource<List<Task>>

    suspend fun getTaskStage(id: Int): Resource<TaskStage>

    suspend fun updateTask(
        token: String,
        id: Int,
        responses: String,
        complete: Boolean
    ): Response<ResponseBody>

    fun makeRequestBody(
        responses: String,
        complete: Boolean
    ): RequestBody

    suspend fun createTask(token: String, stageId: Int): Response<ResponseBody>

    suspend fun getTasksStagesList(
        token: String,
        campaignId: String
    ): Resource<List<TaskStage>>

    suspend fun getNotifications(
        token: String,
        campaignId: String,
        viewed: Boolean = false,
        importance: Int? = null,
    ): Resource<List<Notification>>

    suspend fun getNotification(
        token: String,
        notificationId: Int,
    ): Resource<Notification>

    fun getTokenSynchronously(onError: () -> Unit = {}): String?
}