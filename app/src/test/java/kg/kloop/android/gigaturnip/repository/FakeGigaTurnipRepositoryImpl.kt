package kg.kloop.android.gigaturnip.repository

import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.domain.TaskStage
import kg.kloop.android.gigaturnip.util.Resource
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

class FakeGigaTurnipRepositoryImpl: GigaTurnipRepository {

    var campaignsList = mutableListOf<Campaign>()
    var userSelectableCampaignsList = mutableListOf<Campaign>()
    var notificationsList = mutableListOf<Notification>()

    val tasksList = mutableListOf<Task>()


    override suspend fun getCampaignsList(token: String): Resource<List<Campaign>> {
        return Resource.Success(campaignsList)
    }

    override suspend fun getUserSelectableCampaignsList(token: String): Resource<List<Campaign>> {
        return Resource.Success(userSelectableCampaignsList)
    }

    override suspend fun getCampaign(id: Int): Resource<Campaign> {
        return Resource.Success(campaignsList[id])
    }

    override suspend fun joinCampaign(token: String, campaignId: String): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getTasksList(
        token: String,
        complete: Boolean,
        campaignId: String
    ): Resource<List<Task>> {
        return Resource.Success(tasksList.filter {
            it.isComplete == complete
                    && it.stage.chain.campaignId == campaignId.toInt()
        })
    }

    override suspend fun getTaskById(token: String, id: Int?): Resource<Task> {
        return Resource.Success(tasksList[id!!])
    }

    override suspend fun openPreviousTask(token: String, taskId: Int): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getTasks(token: String, caseId: Int, stageId: Int): Resource<List<Task>> {
        return Resource.Success(tasksList.filter {
            it.caseId == caseId
                    && it.stage.id == stageId.toString()
        })
    }

    override suspend fun getPreviousTasks(token: String, taskId: Int): Resource<List<Task>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskStage(id: Int): Resource<TaskStage> {
        TODO("Not yet implemented")
    }

    override suspend fun updateTask(
        token: String,
        id: Int,
        responses: String,
        complete: Boolean
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun makeRequestBody(responses: String, complete: Boolean): RequestBody {
        TODO("Not yet implemented")
    }

    override suspend fun createTask(token: String, stageId: Int): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getTasksStagesList(
        token: String,
        campaignId: String
    ): Resource<List<TaskStage>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNotifications(
        token: String,
        campaignId: String,
        viewed: Boolean,
        importance: Int?
    ): Resource<List<Notification>> = Resource.Success(notificationsList.filter {
        it.campaignId == campaignId.toInt()
    })

    override suspend fun getNotification(
        token: String,
        notificationId: Int
    ): Resource<Notification> {
        TODO("Not yet implemented")
    }

    override fun getTokenSynchronously(onError: () -> Unit): String? {
        return "token"
    }

}