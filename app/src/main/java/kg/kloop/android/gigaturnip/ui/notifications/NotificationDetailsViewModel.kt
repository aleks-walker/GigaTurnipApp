package kg.kloop.android.gigaturnip.ui.notifications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.ui.auth.getTokenSynchronously
import kg.kloop.android.gigaturnip.util.Constants.NOTIFICATION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class NotificationDetailsUiState(
    val notification: Notification? = null,
    val loading: Boolean = false,
    val error: Boolean = false
)

@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationDetailsUiState())
    val uiState: StateFlow<NotificationDetailsUiState> = _uiState.asStateFlow()

    private val notificationId = savedStateHandle.get<String>(NOTIFICATION_ID)!!

    init {
        refreshNotificationDetails()
    }

    fun refreshNotificationDetails() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val notification = getNotification(notificationId.toInt())
                notification?.let {
                    _uiState.update {
                        it.copy(
                            notification = notification,
                            loading = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun getNotification(id: Int): Notification? {
        val token = getToken()
        return if (token != null) {
            repository.getNotification(
                token = token,
                notificationId = id
            ).data
        } else null
    }

    private fun getToken(): String? = getTokenSynchronously(onError = {
        _uiState.update { it.copy(loading = false, error = true) }
    })
}

