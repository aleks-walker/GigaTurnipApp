package kg.kloop.android.gigaturnip.ui.notifications

import androidx.lifecycle.SavedStateHandle
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

data class NotificationDetailsUiState(
    val notification: Notification? = null,
    val loading: Boolean = false
)

@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val repository: GigaTurnipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationDetailsUiState())
    val uiState: StateFlow<NotificationDetailsUiState> = _uiState.asStateFlow()

    private val notificationId = savedStateHandle.get<String>("id")!!

    init {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val notification = getNotification(notificationId.toInt())
                _uiState.update { it.copy(notification = notification, loading = false) }
            }
        }
    }

    private suspend fun getNotification(id: Int): Notification? =
        repository.getNotification(
            token = getTokenSynchronously()!!,
            notificationId = id
        ).data
}

