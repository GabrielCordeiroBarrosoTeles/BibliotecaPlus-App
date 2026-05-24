package com.bibliotecaplus.presentation.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName: String? = null,
    val userEmail: String? = null,
    val userRole: String? = null,
    val matriculation: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = dataStore.data.map { prefs ->
        ProfileUiState(
            userName = prefs[AuthRepository.USER_NAME_KEY],
            userEmail = prefs[AuthRepository.USER_EMAIL_KEY],
            userRole = prefs[AuthRepository.USER_ROLE_KEY],
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
