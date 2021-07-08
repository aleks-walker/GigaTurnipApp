package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import javax.inject.Inject

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
): ViewModel() {

    fun getCampaigns(token: String) = liveData {
        emit(repository.getCampaignsList(token).data.orEmpty() )
    }
}