package com.bibliotecaplus.data.api.dto

import com.google.gson.annotations.SerializedName

// ─── API Wrapper ──────────────────────────────────────────────────────────────
data class ApiResponse<T>(
    val data: T?,
    val message: String?,
    val timestamp: String?,
    val statusCode: Int?,
)

data class PaginatedData<T>(
    val data: List<T>,
    val meta: PaginationMeta,
)

data class PaginationMeta(
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean,
)

// ─── Auth ─────────────────────────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false,
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val matriculation: String? = null,
    val phone: String? = null,
)

data class LogoutRequest(val refreshToken: String)
data class ForgotPasswordRequest(val email: String)

data class AuthResponse(
    val user: UserDto,
    val tokens: TokenResponse,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
)

data class MessageResponse(val message: String)

// ─── User ─────────────────────────────────────────────────────────────────────
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val matriculation: String?,
    val phone: String?,
    val avatarUrl: String?,
    val isActive: Boolean,
    val maxLoans: Int,
    val loanDays: Int,
    val createdAt: String,
)

// ─── Book ─────────────────────────────────────────────────────────────────────
data class BookDto(
    val id: String,
    val title: String,
    val subtitle: String?,
    val isbn: String?,
    val synopsis: String?,
    val coverUrl: String?,
    val year: Int?,
    val edition: String?,
    val totalCopies: Int,
    val availableCopies: Int,
    val location: String?,
    val author: AuthorDto,
    val publisher: PublisherDto?,
    val categories: List<BookCategoryDto>,
    val createdAt: String,
)

data class AuthorDto(val id: String, val name: String)
data class PublisherDto(val id: String, val name: String)
data class BookCategoryDto(val category: CategoryDto)
data class CategoryDto(val id: String, val name: String, val slug: String)

// ─── Loan ─────────────────────────────────────────────────────────────────────
data class LoanDto(
    val id: String,
    val loanedAt: String,
    val dueDate: String,
    val returnedAt: String?,
    val renewalCount: Int,
    val maxRenewals: Int,
    val status: String,
    val bookCopy: BookCopyDto?,
    val user: UserDto?,
    val fine: FineDto?,
)

data class BookCopyDto(
    val id: String,
    val code: String,
    val book: BookDto?,
)

// ─── Reservation ─────────────────────────────────────────────────────────────
data class CreateReservationRequest(val bookId: String)

data class ReservationDto(
    val id: String,
    val requestedAt: String,
    val expiresAt: String?,
    val status: String,
    val book: BookDto?,
    val user: UserDto?,
)

// ─── Fine ─────────────────────────────────────────────────────────────────────
data class FineDto(
    val id: String,
    val amount: Double,
    val daysLate: Int,
    val status: String,
    val paidAt: String?,
)

// ─── Document ─────────────────────────────────────────────────────────────────
data class DocumentDto(
    val id: String,
    val title: String,
    val description: String?,
    val type: String,
    val fileSize: Int,
    val isPublic: Boolean,
    val downloadCount: Int,
    val authorName: String?,
    val year: Int?,
    val createdAt: String,
)

data class DownloadUrlResponse(val url: String, val expiresIn: Int)

// ─── Notification ─────────────────────────────────────────────────────────────
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val readAt: String?,
    val createdAt: String,
)
