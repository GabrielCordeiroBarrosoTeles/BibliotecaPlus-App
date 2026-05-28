package com.bibliotecaplus.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val statusCode: Int? = null,
)

@Serializable
data class ErrorResponse(
    val message: JsonElement = JsonPrimitive("Erro desconhecido"),
    val error: String? = null,
    val statusCode: Int = 0,
) {
    val messageText: String get() = when (message) {
        is JsonArray -> message.joinToString(", ") { (it as? JsonPrimitive)?.content ?: "" }
        is JsonPrimitive -> message.content
        else -> "Erro desconhecido"
    }
}

@Serializable
data class PaginatedData<T>(
    val data: List<T> = emptyList(),
    val meta: PaginationMeta = PaginationMeta(),
)

@Serializable
data class PaginationMeta(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false,
)

// ─── Auth ─────────────────────────────────────────────────────────────────────
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false,
)

@Serializable
data class LogoutRequest(val refreshToken: String)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val tokens: TokenResponse,
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int = 3600,
)

@Serializable
data class MessageResponse(val message: String = "")

// ─── User ─────────────────────────────────────────────────────────────────────
@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val matriculation: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val maxLoans: Int = 3,
    val loanDays: Int = 14,
    val createdAt: String = "",
)

// ─── Book ─────────────────────────────────────────────────────────────────────
@Serializable
data class BookDto(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val isbn: String? = null,
    val synopsis: String? = null,
    val coverUrl: String? = null,
    val year: Int? = null,
    val edition: String? = null,
    val totalCopies: Int = 0,
    val availableCopies: Int = 0,
    val location: String? = null,
    val author: AuthorDto = AuthorDto("", ""),
    val publisher: PublisherDto? = null,
    val categories: List<BookCategoryDto> = emptyList(),
    val createdAt: String = "",
)

@Serializable
data class AuthorDto(val id: String, val name: String)

@Serializable
data class PublisherDto(val id: String, val name: String)

@Serializable
data class BookCategoryDto(val category: CategoryDto)

@Serializable
data class CategoryDto(val id: String, val name: String, val slug: String)

// ─── Loan ─────────────────────────────────────────────────────────────────────
@Serializable
data class LoanDto(
    val id: String,
    val loanedAt: String,
    val dueDate: String,
    val returnedAt: String? = null,
    val renewalCount: Int = 0,
    val maxRenewals: Int = 2,
    val status: String,
    val bookCopy: BookCopyDto? = null,
    val user: UserDto? = null,
    val fine: FineDto? = null,
)

@Serializable
data class BookCopyDto(
    val id: String,
    val code: String,
    val book: BookDto? = null,
)

// ─── Reservation ─────────────────────────────────────────────────────────────
@Serializable
data class ReservationDto(
    val id: String,
    val requestedAt: String,
    val expiresAt: String? = null,
    val status: String,
    val book: BookDto? = null,
    val user: UserDto? = null,
)

// ─── Fine ─────────────────────────────────────────────────────────────────────
@Serializable
data class FineDto(
    val id: String,
    val amount: Double,
    val daysLate: Int,
    val status: String,
    val paidAt: String? = null,
)

// ─── Document ─────────────────────────────────────────────────────────────────
@Serializable
data class DocumentDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val type: String,
    val fileSize: Int = 0,
    val isPublic: Boolean = true,
    val downloadCount: Int = 0,
    val authorName: String? = null,
    val year: Int? = null,
    val createdAt: String = "",
)

// ─── Notification ─────────────────────────────────────────────────────────────
@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val readAt: String? = null,
    val createdAt: String = "",
)

// ─── Reservation ─────────────────────────────────────────────────────────────
@Serializable
data class CreateReservationRequest(
    val bookId: String,
    val notes: String? = null,
)

// ─── Profile Update ───────────────────────────────────────────────────────────
@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val matriculation: String? = null,
)

// ─── Trending ─────────────────────────────────────────────────────────────────
@Serializable
data class TrendingBookDto(
    val book: BookDto,
    val loanCount: Int = 0,
)
