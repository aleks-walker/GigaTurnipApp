package kg.kloop.android.gigaturnip.data.remote

import kg.kloop.android.gigaturnip.data.models.CampaignDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GigaTurnipApi {

    @GET("allcampaigns")
    suspend fun getCampaignsList(): List<CampaignDto>

    @GET("campaign")
    suspend fun getCampaign(
        @Path("id") id: Int
    ): CampaignDto
}