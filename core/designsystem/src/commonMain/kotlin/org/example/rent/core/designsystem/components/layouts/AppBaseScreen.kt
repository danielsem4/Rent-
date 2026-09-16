package org.example.rent.core.designsystem.components.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.rent.core.designsystem.components.topbar.AppTopBar

/**
 * Standard screen scaffold: an [AppTopBar] with a title and a back/navigation button, plus a column
 * content slot below it.
 */
@Composable
fun AppBaseScreen(
    titleText: String,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = titleText, onNavigationClick = onIconClick)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            content = content,
        )
    }
}
