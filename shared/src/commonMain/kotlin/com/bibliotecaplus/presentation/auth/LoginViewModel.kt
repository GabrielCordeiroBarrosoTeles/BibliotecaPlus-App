package com.bibliotecaplus.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val emailError: String? = null,
)

class LoginViewModel(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(isLoggedIn = auth.isLoggedIn))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String)      { _uiState.update { it.copy(email = value, emailError = null, error = null) } }
    fun onPasswordChange(value: String)   { _uiState.update { it.copy(password = value, error = null) } }
    fun onRememberMeChange(value: Boolean){ _uiState.update { it.copy(rememberMe = value) } }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Informe o e-mail") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = auth.login(state.email.trim(), state.password, state.rememberMe)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao entrar") } },
            )
        }
    }
}
