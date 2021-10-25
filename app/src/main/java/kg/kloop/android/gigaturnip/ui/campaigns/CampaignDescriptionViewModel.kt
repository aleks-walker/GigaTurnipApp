package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class CampaignDescriptionUiState(
    val campaign: Campaign? = null,
    val loading: Boolean = false,
    val joined: Boolean = false

)

@HiltViewModel
class CampaignDescriptionViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignDescriptionUiState())
    val uiState: StateFlow<CampaignDescriptionUiState> = _uiState.asStateFlow()

    fun setCampaign(campaign: Campaign) {
        _uiState.update { it.copy(campaign = campaign) }
    }

    fun setJoined(value: Boolean) {
        _uiState.update { it.copy(joined = value) }
    }


    fun joinCampaign(campaignId: String) {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val response = repository.joinCampaign(getTokenSynchronously()!!, campaignId)
                Timber.d("join response: $response")
                _uiState.update { it.copy(loading = false, joined = true) }
            }
        }
    }
}