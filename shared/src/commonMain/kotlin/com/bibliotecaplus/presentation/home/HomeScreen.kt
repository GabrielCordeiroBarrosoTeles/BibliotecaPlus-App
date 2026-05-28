package com.bibliotecaplus.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bibliotecaplus.data.api.LoanDto
import com.bibliotecaplus.data.api.TrendingBookDto
import com.bibliotecaplus.presentation.books.BookListScreen
import com.bibliotecaplus.presentation.loans.LoansScreen
import com.bibliotecaplus.presentation.profile.ProfileScreen
import org.koin.compose.viewmodel.koinViewModel

private enum class Tab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
) {
    HOME("Início", Icons.Default.Home),
    BOOKS("Livros", Icons.Default.MenuBook),
    LOANS("Empréstimos", Icons.Default.Bookmarks),
    PROFILE("Perfil", Icons.Default.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onBookDetail: (String) -> Unit,
    onNotifications: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(Tab.HOME) }

    LaunchedEffect(uiState.user) {
        if (uiState.user == null && !viewModel.uiState.value.isLoading) onLogout()
    }

    Scaffold(
        topBar = {
            if (currentTab == Tab.HOME) {
                TopAppBar(
                    title = {
                        Text(
                            "Biblioteca+",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotifications > 0) {
                                    Badge { Text(uiState.unreadNotifications.toString()) }
                                }
                            },
                        ) {
                            IconButton(onClick = onNotifications) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        },
        bottomBar = {
            NavigationBar(tonalElevation = 4.dp) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab == Tab.LOANS && uiState.overdueCount > 0) {
                                        Badge { Text(uiState.overdueCount.toString()) }
                                    }
                                },
                            ) {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (currentTab) {
                Tab.HOME    -> HomeTabContent(uiState, viewModel::refresh, onNotifications)
                Tab.BOOKS   -> BookListScreen(onBookClick = onBookDetail)
                Tab.LOANS   -> LoansScreen()
                Tab.PROFILE -> ProfileScreen(
                    onLogout = { viewModel.logout(); onLogout() },
                    onEditProfile = onEditProfile,
                )
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onNotifications: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Greeting ──────────────────────────────────────────────────────────
        item {
            val firstName = uiState.user?.name?.split(" ")?.firstOrNull() ?: "Usuário"
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Olá, $firstName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Acompanhe seus empréstimos e explore o acervo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Stats Row ─────────────────────────────────────────────────────────
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.activeLoans.size,
                    label = "Ativos",
                    icon = Icons.Default.Bookmarks,
                    color = MaterialTheme.colorScheme.primary,
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.dueSoonLoans.size,
                    label = "Vencendo",
                    icon = Icons.Default.Schedule,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.overdueCount,
                    label = "Atrasados",
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        // ── Due Soon Alert ────────────────────────────────────────────────────
        if (uiState.dueSoonLoans.isNotEmpty()) {
            item {
                AlertBanner(
                    icon = Icons.Default.Schedule,
                    message = "Você tem ${uiState.dueSoonLoans.size} livro(s) para devolver em breve. Evite multas!",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }

        // ── Overdue Alert ─────────────────────────────────────────────────────
        if (uiState.overdueCount > 0) {
            item {
                AlertBanner(
                    icon = Icons.Default.Warning,
                    message = "Você tem ${uiState.overdueCount} empréstimo(s) em atraso. Multa em andamento!",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        // ── Loading ───────────────────────────────────────────────────────────
        if (uiState.isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }

        // ── Active Loans ──────────────────────────────────────────────────────
        if (uiState.activeLoans.isNotEmpty()) {
            item {
                Text(
                    text = "Empréstimos ativos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(uiState.activeLoans) { loan -> HomeLoanCard(loan) }
        }

        // ── Empty State ───────────────────────────────────────────────────────
        if (uiState.activeLoans.isEmpty() && !uiState.isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp),
                        )
                        Text("Nenhum empréstimo ativo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Explore o acervo e solicite sua primeira reserva",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── Trending Books ────────────────────────────────────────────────────
        if (uiState.trendingBooks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Em alta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                ) {
                    items(uiState.trendingBooks) { trending ->
                        TrendingBookCard(trending)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatMiniCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun AlertBanner(
    icon: ImageVector,
    message: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = contentColor, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeLoanCard(loan: LoanDto) {
    val title = loan.bookCopy?.book?.title ?: "Livro"
    val author = loan.bookCopy?.book?.author?.name ?: ""
    val isOverdue = loan.status.uppercase() == "OVERDUE"
    val dueDateStr = loan.dueDate.take(10)

    val accentColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = accentColor.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MenuBook, null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (author.isNotBlank()) {
                    Text(author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        if (isOverdue) Icons.Default.Warning else Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        if (isOverdue) "Venceu em $dueDateStr" else "Devolução: $dueDateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                    )
                }
            }
            LoanStatusChip(loan.status)
        }
    }
}

@Composable
private fun TrendingBookCard(trending: TrendingBookDto) {
    val title = trending.book.title
    val author = trending.book.author?.name ?: ""
    val coverUrl = trending.book.coverUrl

    Card(
        modifier = Modifier.width(130.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            ) {
                if (coverUrl != null) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (author.isNotBlank()) {
                    Text(
                        author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (trending.loanCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                        Text(
                            "${trending.loanCount} empréstimos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanStatusChip(status: String) {
    val (label, color) = when (status.uppercase()) {
        "ACTIVE"   -> "Ativo" to MaterialTheme.colorScheme.primary
        "OVERDUE"  -> "Atrasado" to MaterialTheme.colorScheme.error
        "RETURNED" -> "Devolvido" to MaterialTheme.colorScheme.secondary
        "RENEWED"  -> "Renovado" to MaterialTheme.colorScheme.tertiary
        else       -> status to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.12f)) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
