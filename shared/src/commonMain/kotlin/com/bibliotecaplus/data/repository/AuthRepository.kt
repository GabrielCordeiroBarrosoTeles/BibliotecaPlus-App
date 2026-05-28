package com.bibliotecaplus.data.repository

import com.bibliotecaplus.data.api.AuthResponse
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.LoginRequest
import com.bibliotecaplus.data.api.UserDto
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_ACCESS_TOKEN  = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_USER_ID       = "user_id"
private const val KEY_USER_NAME     = "user_name"
private const val KEY_USER_EMAIL    = "user_email"
private const val KEY_USER_ROLE     = "user_role"

class AuthRepository(
    private val api: BibliotecaApiClient,
    private val settings: Settings,
) {
    private val _currentUser = MutableStateFlow<UserDto?>(loadSavedUser())
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    val isLoggedIn: Boolean get() = !settings.getStringOrNull(KEY_ACCESS_TOKEN).isNullOrEmpty()

    suspend fun login(email: String, password: String, rememberMe: Boolean = false): Result<UserDto> {
        val result = api.login(LoginRequest(email, password, rememberMe))
        result.onSuccess { auth ->
            saveSession(auth)
            _currentUser.value = auth.user
        }
        return result.map { it.user }
    }

    fun logout() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_ROLE)
        _currentUser.value = null
    }

    private fun saveSession(auth: AuthResponse) {
        settings.putString(KEY_ACCESS_TOKEN, auth.tokens.accessToken)
        settings.putString(KEY_REFRESH_TOKEN, auth.tokens.refreshToken)
        settings.putString(KEY_USER_ID, auth.user.id)
        settings.putString(KEY_USER_NAME, auth.user.name)
        settings.putString(KEY_USER_EMAIL, auth.user.email)
        settings.putString(KEY_USER_ROLE, auth.user.role)
    }

    private fun loadSavedUser(): UserDto? {
        val id    = settings.getStringOrNull(KEY_USER_ID)    ?: return null
        val name  = settings.getStringOrNull(KEY_USER_NAME)  ?: return null
        val email = settings.getStringOrNull(KEY_USER_EMAIL) ?: return null
        val role  = settings.getStringOrNull(KEY_USER_ROLE)  ?: return null
        return UserDto(id = id, name = name, email = email, role = role)
    }
}
