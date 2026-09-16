package org.example.rent.core.designsystem.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.rent.core.designsystem.theme.AppDimens
import org.example.rent.core.designsystem.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * A standard icon button that respects the minimum touch target. Takes an [icon] slot so callers
 * supply their own vector/painter/text glyph — this keeps the design system free of the
 * material-icons dependency (see [AppTopBar]).
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(AppDimens.minTouchTarget),
        enabled = enabled,
        content = icon,
    )
}

@Preview
@Composable
private fun AppIconButtonPreview() {
    AppTheme {
        AppIconButton(onClick = {}) {
            Text(text = "←", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview
@Composable
private fun AppIconButtonDarkPreview() {
    AppTheme(darkTheme = true) {
        AppIconButton(onClick = {}) {
            Text(text = "←", style = MaterialTheme.typography.titleLarge)
        }
    }
}
