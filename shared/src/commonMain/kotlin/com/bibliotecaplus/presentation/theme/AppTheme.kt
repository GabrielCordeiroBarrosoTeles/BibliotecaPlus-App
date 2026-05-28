package com.bibliotecaplus.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primaryColor      = Color(0xFF1E40AF)
private val primaryContainer  = Color(0xFFDBEAFE)
private val secondaryColor    = Color(0xFF0F766E)
private val surfaceColor      = Color(0xFFF8FAFC)
private val backgroundColor   = Color(0xFFF1F5F9)

private val LightColors = lightColorScheme(
    primary          = primaryColor,
    onPrimary        = Color.White,
    primaryContainer = primaryContainer,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary        = secondaryColor,
    onSecondary      = Color.White,
    surface          = surfaceColor,
    onSurface        = Color(0xFF0F172A),
    background       = backgroundColor,
    onBackground     = Color(0xFF0F172A),
    surfaceVariant   = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    error            = Color(0xFFDC2626),
    onError          = Color.White,
    outline          = Color(0xFFCBD5E1),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
