package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import javax.inject.Inject

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
): ViewModel() {

    val campaigns = liveData {
        emit(repository.getCampaignsList().data.orEmpty() )
    }

    private fun generateCampaigns(): List<Campaign> {
        val result = mutableListOf<Campaign>()
             for (i in 0..4) {
                result.add(Campaign(i.toString(), "Campaign #$i", "Lorem ipsum . . ."))
            }
        return result.toList()
    }
}