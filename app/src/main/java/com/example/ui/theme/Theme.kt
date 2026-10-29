package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KudulaDarkColorScheme = darkColorScheme(
    primary = KudulaElectricBlueLight,
    onPrimary = Color.White,
    primaryContainer = KudulaNavyContainer,
    onPrimaryContainer = KudulaCyanGlow,
    secondary = KudulaVioletLight,
    onSecondary = Color.White,
    secondaryContainer = KudulaVioletDim,
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = KudulaTealLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF134E4A),
    onTertiaryContainer = Color(0xFF99F6E4),
    background = KudulaNavyDark,
    onBackground = TextPrimary,
    surface = KudulaNavy,
    onSurface = TextPrimary,
    surfaceVariant = KudulaNavyContainer,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = Color(0xFF1E293B)
)

val KudulaLightColorScheme = lightColorScheme(
    primary = KudulaElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = KudulaViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = KudulaTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun KudulaTheme(
    darkTheme: Boolean = true, // Default to deep navy cognitive dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KudulaDarkColorScheme else KudulaLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
