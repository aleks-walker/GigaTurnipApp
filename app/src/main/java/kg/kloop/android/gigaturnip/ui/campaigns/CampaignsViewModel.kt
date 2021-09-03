package kg.kloop.android.gigaturnip.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Campaign
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

data class CampaignsUiState(
    val campaigns: List<Campaign> = emptyList(),
    val loading: Boolean = false
) {
    val initialLoad: Boolean
        get() = campaigns.isEmpty() && loading
}

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignsUiState(loading = true))
    val uiState: StateFlow<CampaignsUiState> = _uiState.asStateFlow()

    private fun getToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val result = Tasks.await(user.getIdToken(true));
            return Objects.requireNonNull(result).token!!
        }
        return null
    }

    init {
        refreshCampaigns()
    }

    fun refreshCampaigns() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val tokenDeferred = async { getToken() }
                val token = tokenDeferred.await()
                val result = repository.getCampaignsList(token!!).data.orEmpty()
                _uiState.update {
                    it.copy(campaigns = result, loading = false)
                }

            }
        }
    }
}