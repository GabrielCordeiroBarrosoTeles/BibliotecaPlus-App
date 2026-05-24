package com.bibliotecaplus.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bibliotecaplus.data.api.ApiService
import com.bibliotecaplus.data.api.dto.LoginRequest
import com.bibliotecaplus.data.api.dto.RegisterRequest
import com.bibliotecaplus.data.api.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    val accessToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val isLoggedIn: Flow<Boolean> = accessToken.map { !it.isNullOrEmpty() }

    suspend fun login(email: String, password: String, rememberMe: Boolean = false): Result<UserDto> {
        return try {
            val response = api.login(LoginRequest(email, password, rememberMe))
            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                saveSession(body.tokens.accessToken, body.tokens.refreshToken, body.user)
                Result.success(body.user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Credenciais inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, matriculation: String?): Result<String> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, matriculation))
            if (response.isSuccessful) {
                Result.success(response.body()?.data?.message ?: "Conta criada!")
            } else {
                Result.failure(Exception("Erro ao criar conta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        dataStore.edit { it.clear() }
    }

    private suspend fun saveSession(accessToken: String, refreshToken: String, user: UserDto) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY] = user.id
            prefs[USER_NAME_KEY] = user.name
            prefs[USER_EMAIL_KEY] = user.email
            prefs[USER_ROLE_KEY] = user.role
        }
    }
}
