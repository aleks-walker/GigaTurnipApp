package kg.kloop.android.gigaturnip.data.remote

import kg.kloop.android.gigaturnip.data.models.CampaignDto
import kg.kloop.android.gigaturnip.data.models.TaskDto
import kg.kloop.android.gigaturnip.data.models.TaskStageDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GigaTurnipApi {

    @GET("campaigns")
    suspend fun getCampaignsList(): List<CampaignDto>

    @GET("campaigns/{id}")
    suspend fun getCampaign(
        @Path("id") id: Int
    ): CampaignDto

    @GET("taskstages/{id}")
    suspend fun getTaskStage(
        @Path("id") id: Int
    ): TaskStageDto

    @GET("tasks")
    suspend fun getTasksList(): List<TaskDto>

    @GET("tasks/{id}")
    suspend fun getTask(
        @Path("id") id: Int
    ): TaskDto

}