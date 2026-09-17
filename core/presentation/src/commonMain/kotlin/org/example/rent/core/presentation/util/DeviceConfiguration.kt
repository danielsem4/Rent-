package org.example.rent.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/** Coarse device size bucket derived from the current window width, used for responsive layout. */
enum class DeviceConfiguration {
    MOBILE,
    TABLET,
    DESKTOP;

    companion object {
        fun fromWidthDp(widthDp: Int): DeviceConfiguration = when {
            widthDp < 600 -> MOBILE
            widthDp < 840 -> TABLET
            else -> DESKTOP
        }
    }
}

@Composable
fun currentDeviceConfiguration(): DeviceConfiguration {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
    return DeviceConfiguration.fromWidthDp(widthDp.value.toInt())
}
