package org.example.rent.core.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.rent.core.designsystem.theme.AppDimens
import org.example.rent.core.designsystem.theme.AppSpacing
import org.example.rent.core.designsystem.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

enum class AppButtonStyle {
    /** Filled, primary emphasis. */
    PRIMARY,

    /** Filled with the secondary/accent color. */
    SECONDARY,

    /** Outlined, low emphasis. */
    OUTLINED,
}

/**
 * The standard app button: rounded, comfortably sized, with a built-in loading state. Prefer this
 * over raw Material [Button] so every button shares the same height, shape, and typography.
 *
 * @param isLoading when true, shows a spinner and blocks interaction (button stays [enabled]-styled
 * but non-clickable) so the label doesn't collapse.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.small
    val contentPadding = ButtonDefaults.ContentPadding
    val clickable = enabled && !isLoading

    when (style) {
        AppButtonStyle.OUTLINED -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = AppDimens.buttonHeight),
            enabled = clickable,
            shape = shape,
            contentPadding = contentPadding,
        ) {
            AppButtonContent(text = text, isLoading = isLoading, leadingIcon = leadingIcon)
        }

        else -> Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = AppDimens.buttonHeight),
            enabled = clickable,
            shape = shape,
            colors = style.colors(),
            contentPadding = contentPadding,
        ) {
            AppButtonContent(text = text, isLoading = isLoading, leadingIcon = leadingIcon)
        }
    }
}

@Composable
private fun AppButtonStyle.colors(): ButtonColors = when (this) {
    AppButtonStyle.SECONDARY -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
    )
    else -> ButtonDefaults.buttonColors()
}

@Composable
private fun AppButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: @Composable (() -> Unit)?,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(AppDimens.iconSmall),
            strokeWidth = 2.dp,
            color = LocalContentColor.current,
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            leadingIcon?.invoke()
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview
@Composable
private fun AppButtonPreview() {
    AppTheme {
        AppButton(text = "Continue", onClick = {})
    }
}

@Preview
@Composable
private fun AppButtonDarkPreview() {
    AppTheme(darkTheme = true) {
        AppButton(text = "Continue", onClick = {}, style = AppButtonStyle.SECONDARY)
    }
}

@Preview
@Composable
private fun AppButtonOutlinedLoadingPreview() {
    AppTheme {
        AppButton(text = "Loading", onClick = {}, style = AppButtonStyle.OUTLINED, isLoading = true)
    }
}
