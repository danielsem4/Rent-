package org.example.rent.core.designsystem.components.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.rent.core.designsystem.components.buttons.AppIconButton
import org.example.rent.core.designsystem.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * The standard app top bar: a title with an optional back/navigation action. Kept free of the
 * material-icons dependency by rendering the back affordance as a text chevron (see [AppIconButton]).
 *
 * @param onNavigationClick when non-null, a back chevron is shown that invokes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        navigationIcon = {
            if (onNavigationClick != null) {
                AppIconButton(onClick = onNavigationClick) {
                    Text(text = "←", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        actions = { actions?.invoke() },
    )
}

@Preview
@Composable
private fun AppTopBarPreview() {
    AppTheme {
        AppTopBar(title = "Login", onNavigationClick = {})
    }
}

@Preview
@Composable
private fun AppTopBarNoNavDarkPreview() {
    AppTheme(darkTheme = true) {
        AppTopBar(title = "Listings")
    }
}
