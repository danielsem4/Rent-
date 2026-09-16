package org.example.rent.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Standard component sizing tokens — heights, icon sizes, touch targets, borders, elevation, and
 * layout constraints. Keeps components consistently sized instead of scattering magic numbers.
 */
object AppDimens {
    // Buttons
    /** Standard main button height. */
    val buttonHeight = 52.dp
    /** Compact / secondary button height. */
    val buttonHeightCompact = 44.dp

    // Input fields
    /** Comfortable text-field height / minimum. */
    val textFieldMinHeight = 56.dp

    // Touch targets
    /** Minimum recommended interactive touch target. */
    val minTouchTarget = 48.dp

    // Icons
    /** 20.dp — inline / trailing icons, small spinners. */
    val iconSmall = 20.dp
    /** 24.dp — standard icon size. */
    val iconMedium = 24.dp
    /** 28.dp — prominent icons. */
    val iconLarge = 28.dp

    // Borders
    val borderThin = 1.dp
    val borderThick = 2.dp

    // Elevation — kept subtle and small on purpose.
    val elevationNone = 0.dp
    val elevationSmall = 1.dp
    val elevationMedium = 3.dp

    // Layout
    /** Max width for forms / centered content on tablet & desktop. */
    val formMaxWidth = 480.dp
}
