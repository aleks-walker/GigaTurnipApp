package kg.kloop.android.gigaturnip.data.remote

import kg.kloop.android.gigaturnip.data.models.CampaignDto
import kg.kloop.android.gigaturnip.data.models.TaskDto
import kg.kloop.android.gigaturnip.data.models.TaskStageDto
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GigaTurnipApi {

    @GET("campaigns")
    suspend fun getCampaignsList(
        @Header("Authorization") token: String
    ): List<CampaignDto>

    @GET("campaigns/{id}")
    suspend fun getCampaign(
        @Path("id") id: Int
    ): CampaignDto

    @GET("taskstages/{id}")
    suspend fun getTaskStage(
        @Path("id") id: Int
    ): TaskStageDto

    @GET("taskstages/user_relevant")
    suspend fun getTasksStagesList(
        @Header("Authorization") token: String,
        @Query("is_creatable") isCreatable: Boolean,
        @Query("ranklimits__total_limit") rankLimitsTotalLimit: Int,
        @Query("chain__campaign") campaignId: String
    ): List<TaskStageDto>

    @GET("tasks/user_relevant")
    suspend fun getTasksList(
        @Header("Authorization") token: String,
        @Query("complete") complete: Boolean,
        @Query("stage__chain__campaign") campaignId: String
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

}