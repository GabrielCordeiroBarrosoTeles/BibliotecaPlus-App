package com.bibliotecaplus.presentation.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: BookViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.selectedBook
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookId) {
        if (book == null || book.id != bookId) viewModel.loadBookDetail(bookId)
    }

    // Show reservation feedback via snackbar
    LaunchedEffect(uiState.reservationSuccess, uiState.reservationError) {
        when {
            uiState.reservationSuccess -> {
                snackbarHostState.showSnackbar("Reserva solicitada com sucesso!")
                viewModel.clearReservationState()
            }
            uiState.reservationError != null -> {
                snackbarHostState.showSnackbar(uiState.reservationError!!)
                viewModel.clearReservationState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Detalhe do Livro", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading && book == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (book == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Livro não encontrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Cover ──────────────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                if (!book.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // ── Title + author ─────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (!book.subtitle.isNullOrBlank()) {
                        Text(book.subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(book.author.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }

                // ── Availability ───────────────────────────────────────────────
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (book.availableCopies > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Bookmarks,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = if (book.availableCopies > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        )
                        Text(
                            "${book.availableCopies}/${book.totalCopies} disponíveis",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (book.availableCopies > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }

                // ── Reserve / Request button ───────────────────────────────────
                val role = viewModel.currentUserRole.uppercase()
                val isStudentOrProf = role == "STUDENT" || role == "PROFESSOR"
                val isAdminOrLib = role == "ADMIN" || role == "LIBRARIAN"

                when {
                    isStudentOrProf && book.availableCopies > 0 -> {
                        Button(
                            onClick = { viewModel.reserveBook(book.id) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !uiState.isReserving,
                        ) {
                            if (uiState.isReserving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Solicitar Reserva", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    isStudentOrProf && book.availableCopies == 0 -> {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = false,
                        ) {
                            Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sem exemplares disponíveis")
                        }
                    }
                    isAdminOrLib -> {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar Empréstimo via Monitor")
                        }
                    }
                }

                HorizontalDivider()

                // ── Metadata ───────────────────────────────────────────────────
                InfoRow("Editora", book.publisher?.name)
                InfoRow("ISBN", book.isbn)
                InfoRow("Ano", book.year?.toString())
                InfoRow("Edição", book.edition)
                InfoRow("Localização", book.location)
                val categories = book.categories.mapNotNull { it.category.name }.joinToString(", ")
                if (categories.isNotBlank()) InfoRow("Categorias", categories)

                // ── Synopsis ───────────────────────────────────────────────────
                if (!book.synopsis.isNullOrBlank()) {
                    HorizontalDivider()
                    Text("Sinopse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(book.synopsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
