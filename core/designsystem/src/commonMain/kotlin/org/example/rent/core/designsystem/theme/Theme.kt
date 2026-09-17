package org.example.rent.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/**
 * The single theme wrapper for the whole app. Supplies the brand [ColorScheme], [AppTypography],
 * and [AppShapes] to Material3, and provides the [ExtendedColors] tokens via [LocalExtendedColors].
 *
 * Wrap the app root (and every `@Preview`) in this:
 * ```
 * AppTheme { /* content */ }
 * AppTheme(darkTheme = true) { /* dark preview */ }
 * ```
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
        ) {
            // Paint the themed window background so content sits on `background` in both themes.
            // Without this, MaterialTheme draws nothing and bare screens (no Scaffold) show the
            // light window default — making the dark theme's light text unreadable on light.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                content = content,
            )
        }
    }
}
