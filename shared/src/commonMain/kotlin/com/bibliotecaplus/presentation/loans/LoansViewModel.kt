package com.bibliotecaplus.presentation.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.api.LoanDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoansUiState(
    val isLoading: Boolean = false,
    val loans: List<LoanDto> = emptyList(),
    val statusFilter: String? = null,
    val error: String? = null,
)

class LoansViewModel(
    private val api: BibliotecaApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoansUiState())
    val uiState: StateFlow<LoansUiState> = _uiState.asStateFlow()

    init { loadLoans() }

    fun setStatusFilter(status: String?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
        loadLoans()
    }

    fun renew(loanId: String) {
        viewModelScope.launch {
            api.renewLoan(loanId).onSuccess { loadLoans() }
        }
    }

    fun refresh() = loadLoans()

    private fun loadLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = api.getMyLoans(status = _uiState.value.statusFilter)
            result.fold(
                onSuccess = { paginated ->
                    _uiState.value = _uiState.value.copy(isLoading = false, loans = paginated.data)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                },
            )
        }
    }
}
