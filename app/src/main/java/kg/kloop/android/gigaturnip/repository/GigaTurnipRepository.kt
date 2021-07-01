package kg.kloop.android.gigaturnip.repository

import dagger.hilt.android.scopes.ActivityScoped
import kg.kloop.android.gigaturnip.data.models.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.util.Resource

@ActivityScoped
class GigaTurnipRepository(
    private val api: GigaTurnipApi,
    private val mapper: CampaignDtoMapper
) {
    suspend fun getCampaignsList(): Resource<List<Campaign>> {
        val response = try {
            api.getCampaignsList()
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error(e.message.toString())
        }
        return Resource.Success(mapper.toDomainList(response))
    }

    suspend fun getCampaign(id: Int): Resource<Campaign> {
        val response = try {
            api.getCampaign(id)
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error(e.message.toString())
        }
        return Resource.Success(mapper.mapToDomainModel(response))

    }

}