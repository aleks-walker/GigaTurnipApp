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
    val loading: Boolean = false,
    val error: Boolean = false

) {
    val initialLoad: Boolean
        get() = unreadNotifications.isEmpty() && readNotifications.isEmpty() && loading
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val campaignId = savedStateHandle.get<String>(CAMPAIGN_ID)!!

    init {
        refreshNotifications()
    }

    fun refreshNotifications() {
        loadingState()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val token = getTokenSynchronously { errorState() }
                token?.let {
                    loadNotifications(it)
                }
            }
        }
    }

    private suspend fun loadNotifications(token: String) {
        val unReadNotifications = getNotifications(token, read = false)
        val readNotifications = getNotifications(token, read = true)
        if (unReadNotifications == null || readNotifications == null) {
            errorState()
        } else {
            updateUi(unReadNotifications, readNotifications)
        }
    }

    private fun updateUi(
        unReadNotifications: List<Notification>?,
        readNotifications: List<Notification>?
    ) {
        _uiState.update {
            it.copy(
                unreadNotifications = unReadNotifications.orEmpty(),
                readNotifications = readNotifications.orEmpty(),
                loading = false
            )
        }
    }

    private fun loadingState() {
        _uiState.update { it.copy(loading = true, error = false) }
    }

    private fun errorState() {
        _uiState.update { it.copy(loading = false, error = true) }
    }

    private suspend fun getNotifications(
        token: String,
        read: Boolean
    ): List<Notification>? {
        val response = repository.getNotifications(
            token = token,
            campaignId = campaignId,
            viewed = read
        )
        return if (response.message.isNullOrEmpty()) {
            response.data
        } else null
    }
}