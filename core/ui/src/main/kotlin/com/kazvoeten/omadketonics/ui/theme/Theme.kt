package com.kazvoeten.omadketonics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBackground,
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = DarkTextMain,
    secondary = MacroProtein,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = DarkTextMain,
    tertiary = AppSuccess,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF14532D),
    onTertiaryContainer = DarkTextMain,
    background = DarkBackground,
    onBackground = DarkTextMain,
    surface = DarkSurface,
    onSurface = DarkTextMain,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = DarkTextMuted,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkBackground,
    outline = DarkBorder,
    error = AppError,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = LightTextMain,
    secondary = MacroProtein,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = LightTextMain,
    tertiary = AppSuccess,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCFCE7),
    onTertiaryContainer = LightTextMain,
    background = LightBackground,
    onBackground = LightTextMain,
    surface = LightSurface,
    onSurface = LightTextMain,
    surfaceVariant = LightBorder,
    onSurfaceVariant = LightTextMuted,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightBackground,
    outline = LightBorder,
    error = AppError,
)

@Composable
fun OmadKetonicsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (dynamicColor) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
