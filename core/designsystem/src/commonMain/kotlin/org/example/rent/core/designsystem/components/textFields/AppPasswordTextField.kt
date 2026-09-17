package org.example.rent.core.designsystem.components.textFields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.example.rent.core.designsystem.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * A password variant of [AppTextField] with a built-in show/hide toggle. Visibility state is owned
 * by the caller ([isVisible] / [onToggleVisibility]) so it lives in the ViewModel like all state.
 *
 * @param showLabel label for the toggle when the password is hidden (i.e. tapping reveals it).
 * @param hideLabel label for the toggle when the password is visible.
 */
@Composable
fun AppPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    enabled: Boolean = true,
    showLabel: String = "Show",
    hideLabel: String = "Hide",
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        errorText = errorText,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            TextButton(onClick = onToggleVisibility, enabled = enabled) {
                Text(
                    text = if (isVisible) hideLabel else showLabel,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
    )
}

@Preview
@Composable
private fun AppPasswordTextFieldPreview() {
    AppTheme {
        AppPasswordTextField(
            value = "hunter2",
            onValueChange = {},
            label = "Password",
            isVisible = false,
            onToggleVisibility = {},
        )
    }
}

@Preview
@Composable
private fun AppPasswordTextFieldVisibleDarkPreview() {
    AppTheme(darkTheme = true) {
        AppPasswordTextField(
            value = "hunter2",
            onValueChange = {},
            label = "Password",
            isVisible = true,
            onToggleVisibility = {},
        )
    }
}
