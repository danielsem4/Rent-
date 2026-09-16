package org.example.rent.core.designsystem.components.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.rent.core.designsystem.theme.AppSpacing
import org.example.rent.core.designsystem.theme.AppTheme
import org.example.rent.core.designsystem.theme.extended
import androidx.compose.ui.tooling.preview.Preview

/**
 * A standardized section header: a title with an optional secondary subtitle beneath it. Use to
 * introduce a group of content and give screens a consistent hierarchy.
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }
    }
}

@Preview
@Composable
private fun SectionTitlePreview() {
    AppTheme {
        SectionTitle(
            title = "Your listings",
            subtitle = "3 active · 1 pending",
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview
@Composable
private fun SectionTitleDarkPreview() {
    AppTheme(darkTheme = true) {
        SectionTitle(
            title = "Your listings",
            subtitle = "3 active · 1 pending",
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
