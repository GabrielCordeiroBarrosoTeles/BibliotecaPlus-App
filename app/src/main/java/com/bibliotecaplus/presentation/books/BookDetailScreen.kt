package com.bibliotecaplus.presentation.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: BookViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.books.firstOrNull { it.id == bookId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Detalhes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar") } },
            )
        },
    ) { padding ->
        if (book == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            // Cover
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxWidth().height(280.dp),
                contentScale = ContentScale.Crop,
            )

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title + Author
                Text(book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(book.author.name, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                // Categories
                if (book.categories.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        book.categories.take(3).forEach { cat ->
                            SuggestionChip(onClick = {}, label = { Text(cat.category.name) })
                        }
                    }
                }

                Divider()

                // Info grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoItem("Exemplares", "${book.totalCopies}", Modifier.weight(1f))
                    InfoItem("Disponíveis", "${book.availableCopies}", Modifier.weight(1f))
                    book.year?.let { InfoItem("Ano", "$it", Modifier.weight(1f)) }
                }

                if (book.isbn != null) {
                    InfoItem("ISBN", book.isbn)
                }

                if (book.location != null) {
                    InfoItem("Localização", book.location)
                }

                // Synopsis
                if (!book.synopsis.isNullOrBlank()) {
                    Divider()
                    Text("Sinopse", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(book.synopsis, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }

                Spacer(Modifier.height(8.dp))

                // Reserve button
                if (book.availableCopies == 0) {
                    Button(
                        onClick = { /* TODO: criar reserva */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) {
                        Icon(Icons.Default.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reservar livro")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
