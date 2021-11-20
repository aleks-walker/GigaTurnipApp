package kg.kloop.android.gigaturnip.ui.notifications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kg.kloop.android.gigaturnip.util.Constants.CAMPAIGN_ID
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
    private val repository: GigaTurnipRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val campaignId = savedStateHandle.get<String>(CAMPAIGN_ID)!!

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
        campaignId = campaignId,
        viewed = read
    ).data.orEmpty()
}