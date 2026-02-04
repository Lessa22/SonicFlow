package com.example.sonicflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6B4EFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A35CC),
    onPrimaryContainer = Color(0xFFE5DEFF),
    secondary = Color(0xFFB794F6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF8B5CF6),
    onSecondaryContainer = Color(0xFFF3E5FF),
    tertiary = Color(0xFF7DD3C0),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF2F9D8C),
    onTertiaryContainer = Color(0xFFB2F1E8),
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    errorContainer = Color(0xFFFF3838),
    onErrorContainer = Color(0xFFFFE5E5),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCACACA),
    outline = Color(0xFF3C3C3C)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B4EFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5DEFF),
    onPrimaryContainer = Color(0xFF1F0066),
    secondary = Color(0xFF8B5CF6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5FF),
    onSecondaryContainer = Color(0xFF2D0066),
    tertiary = Color(0xFF2F9D8C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2F1E8),
    onTertiaryContainer = Color(0xFF003E33),
    error = Color(0xFFFF3838),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF8C0000),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun SonicFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}