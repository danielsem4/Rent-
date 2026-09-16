package org.example.rent.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * The one spacing scale for the whole app — use these for padding, margins, and gaps instead of
 * arbitrary dp values. Based on a 4dp grid: 4, 8, 12, 16, 20, 24, 32.
 *
 * These are immutable design constants (they never change with theme), so they're a plain object
 * accessed directly, e.g. `Modifier.padding(AppSpacing.lg)`.
 */
object AppSpacing {
    /** 4.dp */
    val xs = 4.dp
    /** 8.dp */
    val sm = 8.dp
    /** 12.dp */
    val md = 12.dp
    /** 16.dp */
    val lg = 16.dp
    /** 20.dp */
    val xl = 20.dp
    /** 24.dp */
    val xxl = 24.dp
    /** 32.dp */
    val xxxl = 32.dp
}
