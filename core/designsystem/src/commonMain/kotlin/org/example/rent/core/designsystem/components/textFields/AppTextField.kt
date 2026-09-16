package org.example.rent.core.designsystem.components.textFields

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import org.example.rent.core.designsystem.theme.AppDimens
import org.example.rent.core.designsystem.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * The standard text input: a rounded [OutlinedTextField] with consistent shape, height, and inline
 * error support. Prefer this over raw [OutlinedTextField] so every field looks the same.
 *
 * State lives in the caller's ViewModel — this is a stateless render of [value] + [onValueChange].
 *
 * @param errorText when non-null, the field renders in its error state with this supporting text.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val isError = errorText != null
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = AppDimens.textFieldMinHeight),
        enabled = enabled,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        trailingIcon = trailingContent,
        shape = MaterialTheme.shapes.small,
        supportingText = errorText?.let {
            {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        },
    )
}

@Preview
@Composable
private fun AppTextFieldPreview() {
    AppTheme {
        AppTextField(
            value = "eranka@hit.ac.il",
            onValueChange = {},
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
    }
}

@Preview
@Composable
private fun AppTextFieldErrorDarkPreview() {
    AppTheme(darkTheme = true) {
        AppTextField(
            value = "not-an-email",
            onValueChange = {},
            label = "Email",
            errorText = "Enter a valid email address",
        )
    }
}
