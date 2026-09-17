package org.example.rent.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Rent+ brand palette, derived from the logo: a deep navy/royal blue paired with a bright
 * sky-blue accent. These raw tokens feed the Material [ColorScheme]s and the [ExtendedColors]
 * below — screens and components should never reference these constants directly, only
 * [MaterialTheme.colorScheme] and [MaterialTheme.colorScheme.extended].
 */
internal object BrandColors {
    // Primary — deep navy (the house + wordmark)
    val Navy900 = Color(0xFF12296B)
    val Navy700 = Color(0xFF1B3A8C)
    val Navy500 = Color(0xFF2B54B8)
    val Navy300 = Color(0xFF7C97DE)
    val Navy100 = Color(0xFFD9E2F8)

    // Secondary / accent — bright sky blue (the plus marks)
    val Sky600 = Color(0xFF0E8FD6)
    val Sky500 = Color(0xFF33B5F0)
    val Sky300 = Color(0xFF8DD5F7)
    val Sky100 = Color(0xFFDCF1FC)

    // Neutrals
    val White = Color(0xFFFFFFFF)
    val Grey50 = Color(0xFFF6F8FC)
    val Grey100 = Color(0xFFEDF1F7)
    val Grey200 = Color(0xFFDCE3ED)
    val Grey300 = Color(0xFFC3CDDB)
    val Grey400 = Color(0xFF97A3B6)
    val Grey500 = Color(0xFF6B7688)
    val Grey600 = Color(0xFF4A5364)
    val Grey700 = Color(0xFF333B49)
    val Grey800 = Color(0xFF1F252F)
    val Grey900 = Color(0xFF141922)
    val Grey950 = Color(0xFF0D1017)

    // Status
    val Red500 = Color(0xFFD5342F)
    val Red200 = Color(0xFFF7D5D3)
    val Green600 = Color(0xFF1E9E5A)
    val Green200 = Color(0xFFCDEFDB)
    val Amber500 = Color(0xFFE8A317)
}

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = BrandColors.Navy700,
    onPrimary = BrandColors.White,
    primaryContainer = BrandColors.Navy100,
    onPrimaryContainer = BrandColors.Navy900,
    secondary = BrandColors.Sky600,
    onSecondary = BrandColors.White,
    secondaryContainer = BrandColors.Sky100,
    onSecondaryContainer = Color(0xFF063A57),
    tertiary = BrandColors.Sky500,
    onTertiary = BrandColors.White,
    tertiaryContainer = BrandColors.Sky100,
    onTertiaryContainer = Color(0xFF063A57),
    background = BrandColors.Grey50,
    onBackground = BrandColors.Grey900,
    surface = BrandColors.White,
    onSurface = BrandColors.Grey900,
    surfaceVariant = BrandColors.Grey100,
    onSurfaceVariant = BrandColors.Grey600,
    outline = BrandColors.Grey300,
    outlineVariant = BrandColors.Grey200,
    error = BrandColors.Red500,
    onError = BrandColors.White,
    errorContainer = BrandColors.Red200,
    onErrorContainer = Color(0xFF5F0F0C),
    scrim = Color(0xFF000000),
)

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = BrandColors.Navy300,
    onPrimary = BrandColors.Navy900,
    primaryContainer = BrandColors.Navy700,
    onPrimaryContainer = BrandColors.Navy100,
    secondary = BrandColors.Sky300,
    onSecondary = Color(0xFF042A40),
    secondaryContainer = BrandColors.Sky600,
    onSecondaryContainer = BrandColors.Sky100,
    tertiary = BrandColors.Sky300,
    onTertiary = Color(0xFF042A40),
    tertiaryContainer = BrandColors.Sky600,
    onTertiaryContainer = BrandColors.Sky100,
    background = BrandColors.Grey950,
    onBackground = BrandColors.Grey100,
    surface = BrandColors.Grey900,
    onSurface = BrandColors.Grey100,
    surfaceVariant = BrandColors.Grey800,
    onSurfaceVariant = BrandColors.Grey400,
    outline = BrandColors.Grey600,
    outlineVariant = BrandColors.Grey700,
    error = Color(0xFFF2827E),
    onError = Color(0xFF5F0F0C),
    errorContainer = Color(0xFF8A2420),
    onErrorContainer = BrandColors.Red200,
    scrim = Color(0xFF000000),
)

/**
 * Design tokens Material3's [ColorScheme] doesn't cover: finer text tiers, disabled treatments,
 * layered surfaces, and a success status color. Exposed through [MaterialTheme.colorScheme.extended].
 */
data class ExtendedColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,
    val disabledFill: Color,
    val disabledOutline: Color,
    val surfaceLower: Color,
    val surfaceHigher: Color,
    val surfaceOutline: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
)

val LightExtendedColors = ExtendedColors(
    textPrimary = BrandColors.Grey900,
    textSecondary = BrandColors.Grey600,
    textPlaceholder = BrandColors.Grey400,
    textDisabled = BrandColors.Grey400,
    disabledFill = BrandColors.Grey100,
    disabledOutline = BrandColors.Grey200,
    surfaceLower = BrandColors.Grey50,
    surfaceHigher = BrandColors.White,
    surfaceOutline = BrandColors.Grey200,
    success = BrandColors.Green600,
    onSuccess = BrandColors.White,
    successContainer = BrandColors.Green200,
    warning = BrandColors.Amber500,
)

val DarkExtendedColors = ExtendedColors(
    textPrimary = BrandColors.Grey100,
    textSecondary = BrandColors.Grey400,
    textPlaceholder = BrandColors.Grey500,
    textDisabled = BrandColors.Grey600,
    disabledFill = BrandColors.Grey800,
    disabledOutline = BrandColors.Grey700,
    surfaceLower = BrandColors.Grey950,
    surfaceHigher = BrandColors.Grey800,
    surfaceOutline = BrandColors.Grey700,
    success = Color(0xFF56C98A),
    onSuccess = Color(0xFF06351C),
    successContainer = Color(0xFF124D2E),
    warning = Color(0xFFF0BD5A),
)

/**
 * Provides the active [ExtendedColors]. Set by [AppTheme]; reading it outside an [AppTheme]
 * is a programming error.
 */
val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> {
    error("No ExtendedColors provided. Wrap your content in AppTheme { }.")
}

/** Access the Rent+ extended color tokens: `MaterialTheme.colorScheme.extended`. */
val ColorScheme.extended: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
