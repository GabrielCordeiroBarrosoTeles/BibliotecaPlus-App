package com.bibliotecaplus.presentation.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.BookDto
import com.bibliotecaplus.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AvailabilityFilter(val label: String) {
    ALL("Todos"),
    AVAILABLE("Disponíveis"),
    UNAVAILABLE("Indisponíveis"),
}

data class BooksUiState(
    val isLoading: Boolean = false,
    val books: List<BookDto> = emptyList(),
    val selectedBook: BookDto? = null,
    val search: String = "",
    val availabilityFilter: AvailabilityFilter = AvailabilityFilter.ALL,
    val error: String? = null,
    val hasMore: Boolean = false,
    val page: Int = 1,
    val reservationSuccess: Boolean = false,
    val reservationError: String? = null,
    val isReserving: Boolean = false,
)

class BookViewModel(
    private val api: BibliotecaApiClient,
    private val auth: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    val currentUserRole: String get() = auth.currentUser.value?.role ?: ""

    init { loadBooks() }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(search = query, books = emptyList(), page = 1)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadBooks()
        }
    }

    fun onAvailabilityFilterChange(filter: AvailabilityFilter) {
        _uiState.value = _uiState.value.copy(availabilityFilter = filter, books = emptyList(), page = 1)
        loadBooks()
    }

    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        _uiState.value = _uiState.value.copy(page = _uiState.value.page + 1)
        loadBooks(append = true)
    }

    fun selectBook(book: BookDto) { _uiState.value = _uiState.value.copy(selectedBook = book) }
    fun clearSelection()          { _uiState.value = _uiState.value.copy(selectedBook = null) }

    fun clearReservationState() {
        _uiState.value = _uiState.value.copy(reservationSuccess = false, reservationError = null)
    }

    fun reserveBook(bookId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReserving = true, reservationError = null)
            val result = api.createReservation(bookId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isReserving = false, reservationSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isReserving = false, reservationError = e.message ?: "Erro ao solicitar reserva")
                },
            )
        }
    }

    private fun loadBooks(append: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val available = when (_uiState.value.availabilityFilter) {
                AvailabilityFilter.AVAILABLE   -> true
                AvailabilityFilter.UNAVAILABLE -> false
                AvailabilityFilter.ALL         -> null
            }
            val result = api.getBooks(
                page = _uiState.value.page,
                search = _uiState.value.search.takeIf { it.isNotBlank() },
                available = available,
            )
            result.fold(
                onSuccess = { paginated ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        books = if (append) _uiState.value.books + paginated.data else paginated.data,
                        hasMore = paginated.meta.hasNext,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                },
            )
        }
    }

    fun loadBookDetail(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = api.getBook(id)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                selectedBook = result.getOrNull(),
                error = result.exceptionOrNull()?.message,
            )
        }
    }
}
