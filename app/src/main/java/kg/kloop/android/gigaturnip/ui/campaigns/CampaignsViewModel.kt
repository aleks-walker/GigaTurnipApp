package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CampaignsViewModel: ViewModel() {

    private val campaigns = MutableLiveData<List<Campaign>>(generateCampaigns())

    fun getCampaigns(): LiveData<List<Campaign>>{
        return campaigns
    }

    private fun generateCampaigns(): List<Campaign> {
        val result = mutableListOf<Campaign>()
             for (i in 0..4) {
                result.add(Campaign(i.toString(), "Campaign #$i", "Lorem ipsum . . ."))
            }
        return result.toList()
    }
}