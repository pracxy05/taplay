package com.taplay.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TaplayDarkColorScheme = darkColorScheme(
    // Primary — violet
    primary          = TaplayPrimary,
    onPrimary        = Color.White,
    primaryContainer = TaplayPrimaryDim,
    onPrimaryContainer = TaplayTextPrimary,

    // Secondary — cyan
    secondary        = TaplayCyan,
    onSecondary      = TaplayTextInverse,
    secondaryContainer = TaplayCyanDim,
    onSecondaryContainer = TaplayTextPrimary,

    // Background layers
    background       = TaplayBgDark,
    onBackground     = TaplayTextPrimary,
    surface          = TaplaySurface,
    onSurface        = TaplayTextPrimary,
    surfaceVariant   = TaplaySurface2,
    onSurfaceVariant = TaplayTextSecondary,

    // States
    error            = TaplayError,
    onError          = Color.White,
    errorContainer   = TaplayErrorDim,
    onErrorContainer = TaplayTextPrimary,

    // Borders
    outline          = TaplayBorder,
    outlineVariant   = TaplayBorderBright,

    // Scrim
    scrim            = TaplayScrim,

    // Inverse (for snackbars etc)
    inverseSurface   = TaplayTextPrimary,
    inverseOnSurface = TaplayTextInverse,
    inversePrimary   = TaplayPrimaryDim,
)

@Composable
fun TaplayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TaplayDarkColorScheme,
        typography  = TaplayTypography,
        content     = content
    )
}