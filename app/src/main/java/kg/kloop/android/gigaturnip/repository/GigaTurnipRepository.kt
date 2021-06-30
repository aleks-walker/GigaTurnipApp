package kg.kloop.android.gigaturnip.repository

import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.models.Campaign

@ActivityScoped
class GigaTurnipRepository(
    private val api: GigaTurnipApi
) {
    suspend fun getCampaignsList(): List<Campaign> {
        return api.getCampaignsList()
    }

    suspend fun getCampaign(id: Int): Campaign {
        val response = try {
            api.getCampaign(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response as Campaign

    }

}