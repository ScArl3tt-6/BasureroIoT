package com.example.basureroiot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores del mockup de Genesis
private val EsquemaDeColores = lightColorScheme(
    primary = Color(0xFF6E90E8),
    onPrimary = Color.White,
    secondary = Color(0xFF8F7CC9),
    onSecondary = Color.White,
    tertiary = Color(0xFFDCCFF8),
    background = Color(0xFFF5F3FF),
    onBackground = Color(0xFF2B3470),
    surface = Color.White,
    onSurface = Color(0xFF2B3470),
    primaryContainer = Color(0xFFC9DBFF),
    onPrimaryContainer = Color(0xFF2B3470)
)

@Composable
fun BasureroIoTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaDeColores,
        typography = Typography, // viene del archivo Type.kt que creó Android Studio
        content = content
    )
}
