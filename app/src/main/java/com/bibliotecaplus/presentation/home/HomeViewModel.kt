package com.bibliotecaplus.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibliotecaplus.data.local.dao.LoanDao
import com.bibliotecaplus.data.local.entities.LoanEntity
import com.bibliotecaplus.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String? = null,
    val userRole: String? = null,
    val activeLoans: List<LoanEntity> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loanDao: LoanDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Carrega nome do usuário do DataStore
            authRepository.accessToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    // Carrega dados locais
                    loanDao.getActive()
                        .collect { loans ->
                            _uiState.update { it.copy(activeLoans = loans) }
                        }
                }
            }
        }
    }
}
