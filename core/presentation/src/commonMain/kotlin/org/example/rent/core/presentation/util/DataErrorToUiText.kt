package org.example.rent.core.presentation.util

import org.example.rent.core.domain.util.DataError

/**
 * Maps a shared [DataError] to user-facing [UiText].
 *
 * These currently use [UiText.DynamicString] literals so the screen can compile and run before a
 * localized string-resource catalog is set up in `core:presentation`. Upgrade individual cases to
 * `UiText.Resource(Res.string.xxx)` once those resources exist — the call sites do not change.
 */
fun DataError.toUiText(): UiText {
    val message = when (this) {
        DataError.Remote.REQUEST_TIMEOUT -> "The request timed out. Please try again."
        DataError.Remote.NO_INTERNET -> "No internet connection."
        DataError.Remote.UNAUTHORIZED -> "Incorrect credentials. Please try again."
        DataError.Remote.FORBIDDEN -> "You don't have access to this."
        DataError.Remote.NOT_FOUND -> "Not found."
        DataError.Remote.CONFLICT -> "That already exists."
        DataError.Remote.TOO_MANY_REQUESTS -> "Too many attempts. Please wait and try again."
        DataError.Remote.PAYLOAD_TOO_LARGE -> "The request is too large."
        DataError.Remote.SERVER_ERROR -> "Something went wrong on our end."
        DataError.Remote.SERVICE_UNAVAILABLE -> "Service is temporarily unavailable."
        DataError.Remote.SERIALIZATION_ERROR -> "Couldn't read the server response."
        DataError.Remote.BAD_REQUEST -> "The request was invalid."
        DataError.Local.DISK_FULL -> "Not enough storage space."
        DataError.Local.NOT_FOUND -> "Not found."
        else -> "Something went wrong. Please try again."
    }
    return UiText.DynamicString(message)
}
