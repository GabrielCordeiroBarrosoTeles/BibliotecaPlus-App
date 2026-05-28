package com.bibliotecaplus.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.UserDto
import com.bibliotecaplus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val error: String? = null,
)

class ProfileViewModel(
    private val auth: AuthRepository,
    private val api: BibliotecaApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(user = auth.currentUser.value))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun logout() = auth.logout()

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = api.getMe()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = result.getOrElse { auth.currentUser.value },
                error = result.exceptionOrNull()?.message,
            )
        }
    }
}
