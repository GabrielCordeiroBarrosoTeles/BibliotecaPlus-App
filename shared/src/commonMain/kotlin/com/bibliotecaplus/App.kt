package com.bibliotecaplus

import androidx.compose.runtime.Composable
import com.bibliotecaplus.data.repository.AuthRepository
import com.bibliotecaplus.presentation.navigation.AppNavGraph
import com.bibliotecaplus.presentation.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun SharedApp() {
    val auth: AuthRepository = koinInject()
    AppTheme {
        AppNavGraph(startLoggedIn = auth.isLoggedIn)
    }
}
