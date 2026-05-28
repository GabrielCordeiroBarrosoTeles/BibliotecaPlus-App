package com.bibliotecaplus.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationDto> = emptyList(),
    val error: String? = null,
)

class NotificationsViewModel(
    private val api: BibliotecaApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = api.getNotifications()
            result.fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, notifications = list)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                },
            )
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            api.markNotificationRead(id)
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { n ->
                    if (n.id == id && n.readAt == null) n.copy(readAt = "read") else n
                },
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            api.markAllNotificationsRead()
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { n ->
                    if (n.readAt == null) n.copy(readAt = "read") else n
                },
            )
        }
    }
}
