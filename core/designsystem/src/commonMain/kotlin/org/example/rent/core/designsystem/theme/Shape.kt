package org.example.rent.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The app's rounded shape scale, wired into [MaterialTheme.shapes]. Components should reference
 * `MaterialTheme.shapes.small/medium/large/…` rather than building ad-hoc `RoundedCornerShape`s.
 *
 * - extraSmall (8) — chips, small tags
 * - small (12) — buttons, text fields
 * - medium (16) — inputs, menus, small containers
 * - large (20) — cards
 * - extraLarge (28) — dialogs, bottom sheets
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
