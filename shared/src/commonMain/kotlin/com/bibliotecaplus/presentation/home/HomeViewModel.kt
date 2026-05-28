package com.bibliotecaplus.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.LoanDto
import com.bibliotecaplus.data.api.NotificationDto
import com.bibliotecaplus.data.api.TrendingBookDto
import com.bibliotecaplus.data.api.UserDto
import com.bibliotecaplus.data.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val activeLoans: List<LoanDto> = emptyList(),
    val dueSoonLoans: List<LoanDto> = emptyList(),
    val overdueCount: Int = 0,
    val unreadNotifications: Int = 0,
    val trendingBooks: List<TrendingBookDto> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(
    private val auth: AuthRepository,
    private val api: BibliotecaApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(user = auth.currentUser.value))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val loansDeferred    = async { api.getMyLoans(limit = 20, status = "ACTIVE") }
            val overdueDeferred  = async { api.getMyLoans(limit = 20, status = "OVERDUE") }
            val notifDeferred    = async { api.getNotifications() }
            val trendingDeferred = async { api.getTrendingBooks(6) }

            val activeLoans   = loansDeferred.await().getOrDefault(null)?.data ?: emptyList()
            val overdueLoans  = overdueDeferred.await().getOrDefault(null)?.data ?: emptyList()
            val notifications = notifDeferred.await().getOrDefault(null) ?: emptyList()
            val trending      = trendingDeferred.await().getOrDefault(null) ?: emptyList()

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val threshold = today.plus(kotlinx.datetime.DatePeriod(days = 3))

            val dueSoon = activeLoans.filter { loan ->
                try {
                    val d = loan.dueDate.take(10)
                    val parts = d.split("-")
                    val loanDate = kotlinx.datetime.LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                    loanDate <= threshold
                } catch (_: Exception) { false }
            }

            val unread = notifications.count { it.readAt == null }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = auth.currentUser.value,
                activeLoans = activeLoans,
                dueSoonLoans = dueSoon,
                overdueCount = overdueLoans.size,
                unreadNotifications = unread,
                trendingBooks = trending,
            )
        }
    }

    fun refresh() = loadData()

    fun logout() = auth.logout()
}
