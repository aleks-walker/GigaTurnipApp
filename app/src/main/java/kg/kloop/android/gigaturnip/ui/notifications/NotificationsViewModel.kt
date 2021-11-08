package kg.kloop.android.gigaturnip.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class NotificationsUiState(
    val unreadNotifications: List<Notification> = emptyList(),
    val readNotifications: List<Notification> = emptyList(),
    val loading: Boolean = false

) {
    val initialLoad: Boolean
        get() = unreadNotifications.isEmpty() && readNotifications.isEmpty() && loading
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _campaignId = MutableLiveData<String>()
    val campaignId: LiveData<String> = _campaignId
    fun setCampaignId(value: String) {
        _campaignId.value = value
    }

    init {
        refreshNotifications()
    }

    fun refreshNotifications() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val unReadNotifications = getNotifications(read = false)
                val readNotifications = getNotifications(read = true)
                _uiState.update {
                    it.copy(
                        unreadNotifications = unReadNotifications,
                        readNotifications = readNotifications,
                        loading = false
                    )
                }
            }
        }
    }

    private suspend fun getNotifications(read: Boolean) = repository.getNotifications(
        token = getTokenSynchronously()!!,
        campaignId = _campaignId.value!!,
        viewed = read
    ).data.orEmpty()
}