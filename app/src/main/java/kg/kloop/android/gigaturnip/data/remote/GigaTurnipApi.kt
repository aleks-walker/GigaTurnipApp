package kg.kloop.android.gigaturnip.data.remote

import kg.kloop.android.gigaturnip.data.models.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GigaTurnipApi {

    @GET("campaigns/list_user_campaigns")
    suspend fun getCampaignsList(
        @Header("Authorization") token: String
    ): List<CampaignDto>

    @GET("campaigns/{id}")
    suspend fun getCampaign(
        @Path("id") id: Int
    ): CampaignDto

    @GET("campaigns/list_user_selectable")
    suspend fun getUserSelectableCampaignsList(
        @Header("Authorization") token: String
    ): List<CampaignDto>

    @GET("campaigns/{id}/join_campaign")
    suspend fun joinCampaign(
        @Header("Authorization") token: String,
        @Path("id") campaignId: String
    ): Response<ResponseBody>

    @GET("taskstages/{id}")
    suspend fun getTaskStage(
        @Path("id") id: Int
    ): TaskStageDto

    @GET("taskstages/user_relevant")
    suspend fun getTasksStagesList(
        @Header("Authorization") token: String,
        @Query("chain__campaign") campaignId: String
    ): List<TaskStageDto>

    @GET("tasks/user_relevant")
    suspend fun getTasksList(
        @Header("Authorization") token: String,
        @Query("complete") complete: Boolean,
        @Query("stage__chain__campaign") campaignId: String
    ): List<TaskDto>

    @GET("tasks/{id}/list_displayed_previous")
    suspend fun getPreviousTasksList(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<TaskDto>

    @GET("tasks/{id}")
    suspend fun getTaskById(
        @Header("Authorization") token: String,
        @Path("id") id: Int?,
    ): TaskDto

    @GET("tasks/")
    suspend fun getTasks(
        @Header("Authorization") token: String,
        @Query("case") caseId: Int,
        @Query("stage") stageId: Int
    ): List<TaskDto>

    @GET("tasks/{id}/open_previous/")
    suspend fun openPreviousTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: Int
    ): Response<ResponseBody>

    @PATCH("tasks/{id}/")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @POST("taskstages/{id}/create_task/")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ResponseBody>

    @GET("notifications/list_user_notifications/")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("campaign") campaignId: String,
        @Query("viewed") viewed: Boolean,
        @Query("importance") importance: Int?
    ): PaginationDto<NotificationDto>

    @GET("notifications/{id}")
    suspend fun getNotification(
        @Header("Authorization") token: String,
        @Path("id") notificationId: Int
    ): NotificationDto

}