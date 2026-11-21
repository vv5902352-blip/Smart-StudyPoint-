package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val SmartStudyLightColors = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = SkyLight,
    onPrimaryContainer = NavyDeep,
    secondary = NavyDeep,
    onSecondary = PureWhite,
    secondaryContainer = SkyTint,
    onSecondaryContainer = NavyDeep,
    tertiary = RoyalBlueBright,
    onTertiary = PureWhite,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = NavyDeep,
    background = PureWhite,
    onBackground = SlateInk,
    surface = PureWhite,
    onSurface = SlateInk,
    surfaceVariant = SkyIce,
    onSurfaceVariant = SlateMuted,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = SkyIce,
    surfaceContainer = SkyTint,
    surfaceContainerHigh = SkyLight,
    outline = SlateBorder,
    outlineVariant = Color(0xFFE5EEFA),
    error = CrimsonError,
    onError = PureWhite,
    errorContainer = CrimsonSoft,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val SmartStudyDarkColors = darkColorScheme(
    primary = SkyAccent,
    onPrimary = NavyMidnight,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = PureWhite,
    secondary = SkySoftText,
    onSecondary = NavyMidnight,
    secondaryContainer = NavyCard,
    onSecondaryContainer = SkyLight,
    tertiary = Color(0xFF38BDF8),
    onTertiary = NavyMidnight,
    tertiaryContainer = Color(0xFF2B3E6B),
    onTertiaryContainer = SkyLight,
    background = NavyMidnight,
    onBackground = Color(0xFFF1F5F9),
    surface = NavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NavyCard,
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceContainerLowest = Color(0xFF070E20),
    surfaceContainerLow = NavySurface,
    surfaceContainer = NavyCard,
    surfaceContainerHigh = Color(0xFF23386B),
    outline = DarkBorder,
    outlineVariant = Color(0xFF1F325E),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = CrimsonSoft
)

val SmartStudyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SmartStudyPointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SmartStudyDarkColors else SmartStudyLightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SmartStudyTypography,
        shapes = SmartStudyShapes,
        content = content
    )
}
