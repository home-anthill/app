package eu.homeanthill.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.homeanthill.api.model.ProfileNotification
import eu.homeanthill.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationsViewModel(
  private val notificationsRepository: NotificationsRepository,
) : ViewModel() {
  private val _notificationsUiState =
    MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
  val notificationsUiState: StateFlow<NotificationsUiState> =
    _notificationsUiState.asStateFlow()

  private val dtf = DateTimeFormatter
    .ofPattern("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    .withZone(ZoneId.systemDefault())

  init {
    loadNotifications()
  }

  fun loadNotifications() {
    viewModelScope.launch {
      _notificationsUiState.value = NotificationsUiState.Loading
      try {
        _notificationsUiState.value =
          NotificationsUiState.Idle(notificationsRepository.repoGetNotifications())
      } catch (err: Exception) {
        _notificationsUiState.value =
          NotificationsUiState.Error(err.message ?: "Cannot load notifications")
      }
    }
  }

  fun getPrettyDateFromUnixEpoch(unixEpoch: Long): String {
    return dtf.format(Instant.ofEpochMilli(unixEpoch))
  }

  sealed class NotificationsUiState {
    data class Idle(val notifications: List<ProfileNotification>) : NotificationsUiState()
    data object Loading : NotificationsUiState()
    data class Error(val errorMessage: String) : NotificationsUiState()
  }
}
