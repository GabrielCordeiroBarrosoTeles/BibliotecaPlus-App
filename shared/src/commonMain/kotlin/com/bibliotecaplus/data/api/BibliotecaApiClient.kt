package com.bibliotecaplus.data.api

import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private const val ACCESS_TOKEN_KEY = "access_token"

class BibliotecaApiClient(
    private val baseUrl: String,
    private val settings: Settings,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.INFO }
        install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            val token = settings.getStringOrNull(ACCESS_TOKEN_KEY)
            if (!token.isNullOrEmpty()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────
    suspend fun login(body: LoginRequest): Result<AuthResponse> = safeCall {
        client.post("auth/login") { setBody(body) }.body<AuthResponse>()
    }

    suspend fun logout(refreshToken: String): Result<Unit> = safeCall {
        client.post("auth/logout") { setBody(LogoutRequest(refreshToken)) }
        Unit
    }

    // ── Books ─────────────────────────────────────────────────────────────────
    suspend fun getBooks(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        available: Boolean? = null,
    ): Result<PaginatedData<BookDto>> = safeCall {
        client.get("books") {
            parameter("page", page)
            parameter("limit", limit)
            if (search != null) parameter("search", search)
            if (available != null) parameter("available", available)
        }.body<PaginatedData<BookDto>>()
    }

    suspend fun getBook(id: String): Result<BookDto> = safeCall {
        client.get("books/$id").body<BookDto>()
    }

    suspend fun getTrendingBooks(limit: Int = 6): Result<List<TrendingBookDto>> = safeCall {
        client.get("books/trending") { parameter("limit", limit) }.body<List<TrendingBookDto>>()
    }

    // ── Loans ─────────────────────────────────────────────────────────────────
    suspend fun getMyLoans(
        page: Int = 1,
        limit: Int = 20,
        status: String? = null,
    ): Result<PaginatedData<LoanDto>> = safeCall {
        client.get("loans") {
            parameter("page", page)
            parameter("limit", limit)
            if (status != null) parameter("status", status)
        }.body<PaginatedData<LoanDto>>()
    }

    suspend fun renewLoan(id: String): Result<LoanDto> = safeCall {
        client.post("loans/$id/renew").body<LoanDto>()
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    suspend fun getMe(): Result<UserDto> = safeCall {
        client.get("users/me").body<UserDto>()
    }

    // ── Reservations ─────────────────────────────────────────────────────────
    suspend fun createReservation(bookId: String, notes: String? = null): Result<ReservationDto> = safeCall {
        client.post("reservations") { setBody(CreateReservationRequest(bookId, notes)) }.body<ReservationDto>()
    }

    suspend fun cancelReservation(id: String): Result<Unit> = safeCall {
        client.delete("reservations/$id")
        Unit
    }

    // ── Profile Update ────────────────────────────────────────────────────────
    suspend fun updateMe(dto: UpdateUserRequest): Result<UserDto> = safeCall {
        client.patch("users/me") { setBody(dto) }.body<UserDto>()
    }

    // ── Notifications ─────────────────────────────────────────────────────────
    suspend fun getNotifications(): Result<List<NotificationDto>> = safeCall {
        client.get("notifications").body<List<NotificationDto>>()
    }

    suspend fun markNotificationRead(id: String): Result<Unit> = safeCall {
        client.post("notifications/$id/read")
        Unit
    }

    suspend fun markAllNotificationsRead(): Result<Unit> = safeCall {
        client.post("notifications/read-all")
        Unit
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: ResponseException) {
        val text = try { e.response.bodyAsText() } catch (_: Exception) { "" }
        val msg = if (text.isNotBlank()) {
            try { json.decodeFromString<ErrorResponse>(text).messageText }
            catch (_: Exception) { "HTTP ${e.response.status.value}" }
        } else {
            "HTTP ${e.response.status.value}"
        }
        Result.failure(Exception(msg))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
