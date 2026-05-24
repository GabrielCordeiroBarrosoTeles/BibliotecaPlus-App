package com.bibliotecaplus.presentation.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.api.ApiService
import com.bibliotecaplus.data.local.dao.LoanDao
import com.bibliotecaplus.data.local.entities.LoanEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoansUiState(
    val loans: List<LoanEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val api: ApiService,
    private val loanDao: LoanDao,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val uiState: StateFlow<LoansUiState> = combine(
        loanDao.getAll(),
        _isLoading,
    ) { loans, loading ->
        LoansUiState(loans = loans, isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoansUiState(isLoading = true))

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getMyLoans()
                if (response.isSuccessful) {
                    val loansDto = response.body()?.data?.data ?: emptyList()
                    val entities = loansDto.map { loan ->
                        LoanEntity(
                            id = loan.id,
                            bookTitle = loan.bookCopy?.book?.title ?: "Livro",
                            bookCoverUrl = loan.bookCopy?.book?.coverUrl,
                            loanedAt = loan.loanedAt,
                            dueDate = loan.dueDate,
                            returnedAt = loan.returnedAt,
                            status = loan.status,
                            renewalCount = loan.renewalCount,
                            maxRenewals = loan.maxRenewals,
                        )
                    }
                    loanDao.upsertAll(entities)
                }
            } catch (e: Exception) {
                // Mostra dados do cache offline
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renew(loanId: String) {
        viewModelScope.launch {
            try {
                api.renewLoan(loanId)
                refresh()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
