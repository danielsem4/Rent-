package org.example.rent.core.presentation.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Wraps a string that originates from — or could originate from — a Compose Multiplatform
 * string resource. Use for user-facing text that may be localized (e.g. error messages).
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class Resource(
        val id: StringResource,
        val args: Array<Any> = arrayOf(),
    ) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is Resource -> stringResource(resource = id, *args)
    }

    suspend fun asStringAsync(): String = when (this) {
        is DynamicString -> value
        is Resource -> getString(resource = id, *args)
    }
}
