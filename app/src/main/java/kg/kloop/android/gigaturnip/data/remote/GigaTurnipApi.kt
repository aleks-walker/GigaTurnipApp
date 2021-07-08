package kg.kloop.android.gigaturnip.data.remote

import kg.kloop.android.gigaturnip.data.models.CampaignDto
import kg.kloop.android.gigaturnip.data.models.TaskDto
import kg.kloop.android.gigaturnip.data.models.TaskStageDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("tasks")
    suspend fun getTasksList(
        @Header("Authorization") token: String,
        @Query("assignee__username") userId: String,
        @Query("complete") complete: Boolean
    ): List<TaskDto>

    @GET("tasks/{id}")
    suspend fun getTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): TaskDto

}