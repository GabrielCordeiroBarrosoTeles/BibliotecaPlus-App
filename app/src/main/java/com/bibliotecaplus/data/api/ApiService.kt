package com.bibliotecaplus.data.api

import com.bibliotecaplus.data.api.dto.*
import retrofit2.Response
import retrofit2.http.*

// ─────────────────────────────────────────────────────────────────────────────
//  Biblioteca+ — Retrofit API Service
// ─────────────────────────────────────────────────────────────────────────────

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<MessageResponse>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<ApiResponse<TokenResponse>>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<ApiResponse<MessageResponse>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiResponse<MessageResponse>>

    // ── Books ─────────────────────────────────────────────────────────────────
    @GET("books")
    suspend fun getBooks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("available") available: Boolean? = null,
    ): Response<ApiResponse<PaginatedData<BookDto>>>

    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: String): Response<ApiResponse<BookDto>>

    // ── Loans ─────────────────────────────────────────────────────────────────
    @GET("loans")
    suspend fun getMyLoans(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
    ): Response<ApiResponse<PaginatedData<LoanDto>>>

    @POST("loans/{id}/renew")
    suspend fun renewLoan(@Path("id") id: String): Response<ApiResponse<LoanDto>>

    // ── Reservations ──────────────────────────────────────────────────────────
    @GET("reservations")
    suspend fun getMyReservations(): Response<ApiResponse<PaginatedData<ReservationDto>>>

    @POST("reservations")
    suspend fun createReservation(@Body body: CreateReservationRequest): Response<ApiResponse<ReservationDto>>

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: String): Response<ApiResponse<MessageResponse>>

    // ── Documents ─────────────────────────────────────────────────────────────
    @GET("documents")
    suspend fun getDocuments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
    ): Response<ApiResponse<PaginatedData<DocumentDto>>>

    @GET("documents/{id}/download")
    suspend fun getDocumentDownloadUrl(@Path("id") id: String): Response<ApiResponse<DownloadUrlResponse>>

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(@Query("unread") unread: Boolean = false): Response<ApiResponse<List<NotificationDto>>>

    @POST("notifications/read-all")
    suspend fun markAllRead(): Response<ApiResponse<MessageResponse>>

    // ── User ──────────────────────────────────────────────────────────────────
    @GET("users/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>
}
