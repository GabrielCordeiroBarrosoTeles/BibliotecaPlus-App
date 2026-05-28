package com.bibliotecaplus.presentation.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bibliotecaplus.data.api.LoanDto
import org.koin.compose.viewmodel.koinViewModel

private val filters = listOf(null to "Todos", "ACTIVE" to "Ativos", "OVERDUE" to "Atrasados", "RETURNED" to "Devolvidos")

@Composable
fun LoansScreen(
    viewModel: LoansViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filters) { (status, label) ->
                FilterChip(
                    selected = uiState.statusFilter == status,
                    onClick = { viewModel.setStatusFilter(status) },
                    label = { Text(label) },
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (uiState.loans.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Bookmarks,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Nenhum empréstimo encontrado",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.loans, key = { it.id }) { loan ->
                LoanItem(loan = loan, onRenew = { viewModel.renew(loan.id) })
            }
        }
    }
}

@Composable
private fun LoanItem(loan: LoanDto, onRenew: () -> Unit) {
    val book = loan.bookCopy?.book
    val title = book?.title ?: "Livro"
    val author = book?.author?.name ?: ""
    val canRenew = loan.status == "ACTIVE" && loan.renewalCount < loan.maxRenewals

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    if (author.isNotBlank()) {
                        Text(
                            author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                StatusChip(loan.status)
            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LoanInfoColumn("Retirada", loan.loanedAt.take(10))
                LoanInfoColumn("Devolução", loan.dueDate.take(10))
                LoanInfoColumn("Renovações", "${loan.renewalCount}/${loan.maxRenewals}")
            }

            if (loan.fine != null && loan.fine.status != "PAID") {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        "Multa pendente: R$ ${"%.2f".format(loan.fine.amount)} (${loan.fine.daysLate} dias)",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            if (canRenew) {
                OutlinedButton(
                    onClick = onRenew,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Renovar empréstimo")
                }
            }
        }
    }
}

@Composable
private fun LoanInfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status.uppercase()) {
        "ACTIVE"   -> "Ativo" to MaterialTheme.colorScheme.primary
        "OVERDUE"  -> "Atrasado" to MaterialTheme.colorScheme.error
        "RETURNED" -> "Devolvido" to MaterialTheme.colorScheme.secondary
        else       -> status to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
