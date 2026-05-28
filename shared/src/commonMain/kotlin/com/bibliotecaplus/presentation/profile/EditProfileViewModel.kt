package com.bibliotecaplus.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.UpdateUserRequest
import com.bibliotecaplus.data.api.UserDto
import com.bibliotecaplus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val matriculation: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class EditProfileViewModel(
    private val auth: AuthRepository,
    private val api: BibliotecaApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = api.getMe()
            val user = result.getOrNull() ?: auth.currentUser.value
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    name = user.name,
                    phone = user.phone ?: "",
                    matriculation = user.matriculation ?: "",
                    email = user.email,
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun onNameChange(value: String)          { _uiState.value = _uiState.value.copy(name = value, error = null) }
    fun onPhoneChange(value: String)         { _uiState.value = _uiState.value.copy(phone = value, error = null) }
    fun onMatriculationChange(value: String) { _uiState.value = _uiState.value.copy(matriculation = value, error = null) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Nome não pode ser vazio")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            val result = api.updateMe(
                UpdateUserRequest(
                    name = state.name.trim(),
                    phone = state.phone.trim().takeIf { it.isNotBlank() },
                    matriculation = state.matriculation.trim().takeIf { it.isNotBlank() },
                ),
            )
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, success = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = e.message ?: "Erro ao salvar")
                },
            )
        }
    }
}
