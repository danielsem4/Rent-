package org.example.rent.core.designsystem.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.example.rent.core.designsystem.theme.AppDimens
import org.example.rent.core.designsystem.theme.AppSpacing
import org.example.rent.core.designsystem.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * A rounded content container with subtle elevation and standard inner padding. Use to group
 * related information — not to wrap every element. Content is laid out in a [ColumnScope] with a
 * default vertical gap of [AppSpacing.md].
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = AppSpacing.lg,
    verticalSpacing: Dp = AppSpacing.md,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.elevationSmall),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content,
        )
    }
}

@Preview
@Composable
private fun AppCardPreview() {
    AppTheme {
        AppCard(modifier = Modifier.padding(AppSpacing.lg)) {
            Text("Sunny 2BR Apartment", style = MaterialTheme.typography.titleMedium)
            Text(
                "Downtown · Available now",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun AppCardDarkPreview() {
    AppTheme(darkTheme = true) {
        AppCard(modifier = Modifier.padding(AppSpacing.lg)) {
            Text("Sunny 2BR Apartment", style = MaterialTheme.typography.titleMedium)
            Text(
                "Downtown · Available now",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
