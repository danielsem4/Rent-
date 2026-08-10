---
name: kmp-error-handling
description: |
  Generic Result wrapper, error types, and extension helpers for Kotlin Multiplatform (Android + iOS) - Result<T, E>, DataError, EmptyResult, map, onSuccess, onFailure, and platform-aware safeCall. Use this skill whenever defining error types, creating a Result wrapper, handling success/failure flows, mapping errors, or working with typed errors anywhere in a KMP project. Trigger on phrases like "Result wrapper", "error handling", "DataError", "onSuccess", "onFailure", "EmptyResult", "map result", "error type", "validation error", "safeCall", or "typed errors".
---

# KMP Error Handling

## Result Wrapper (`core:domain`, `commonMain`)

A generic, typed Result that works across all layers — data, domain, presentation, validation, anywhere a function can succeed or fail with a typed error. Pure Kotlin, no platform dependencies.

```kotlin
interface Error

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Failure<out E : Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>
```

---

## Extension Helpers (`core:domain`, `commonMain`)

These live alongside the `Result` definition:

```kotlin
inline fun <T, E : Error, R> Result<T, E>.map(
    map: (T) -> R
): Result<R, E> {
    return when (this) {
        is Result.Failure -> Result.Failure(error)
        is Result.Success -> Result.Success(map(this.data))
    }
}

inline fun <T, E : Error> Result<T, E>.onSuccess(
    action: (T) -> Unit
): Result<T, E> {
    return when (this) {
        is Result.Failure -> this
        is Result.Success -> {
            action(this.data)
            this
        }
    }
}

inline fun <T, E : Error> Result<T, E>.onFailure(
    action: (E) -> Unit
): Result<T, E> {
    return when (this) {
        is Result.Failure -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}
```

All helpers return `Result` so they can be chained:
```kotlin
repository.saveNote(note)
    .onSuccess { /* update UI */ }
    .onFailure { /* show error */ }
    .asEmptyResult()
```

---

## Shared Error Types (`core:domain`, `commonMain`)

### DataError

```kotlin
sealed interface DataError : Error {
    enum class Remote : DataError {
        BAD_REQUEST,
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        SERIALIZATION_ERROR,
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN
    }
}
```

### Feature-Specific Errors

Features define their own error types by implementing `Error`:

```kotlin
enum class PasswordValidationError : Error {
    TOO_SHORT,
    NO_UPPERCASE,
    NO_DIGIT
}

fun validatePassword(pw: String): EmptyResult<PasswordValidationError>
```

Multiple validation errors are not supported — always return a single error type per Result.

---

## Exception Handling Philosophy

Never throw exceptions for expected failures — always return `Result.Failure`. Catch exceptions at the layer that is responsible for the exception:

| Exception origin | Catch in | KMP example |
|---|---|---|
| HTTP / network | Data layer | `IOException` → `DataError.Remote.NO_INTERNET` |
| Database / disk | Data layer | `SQLiteException` / `NSError` → `DataError.Local.DISK_FULL` |
| Business logic | Domain layer | Invalid input → `Result.Failure(ValidationError.TOO_SHORT)` |
| Presentation | Presentation layer | Catch and map to `Result.Failure` at that layer |

The layer that owns the exception catches it and converts it to a typed `Result.Failure`. Upper layers never see raw exceptions for expected failures.

---

## Safe Call Helpers (`core:data`, `commonMain`)

On KMP, JVM-specific exception classes like `UnresolvedAddressException` don't exist in `commonMain`. Ktor normalizes network failures to `IOException` (`kotlinx.io.IOException`) across targets, so use that in the common `safeCall`:

```kotlin
// core:data/commonMain
suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Remote> {
    return safeCall {
        get {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request
): Result<Response, DataError.Remote> {
    return safeCall {
        post {
            url(constructRoute(route))
            setBody(body)
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Remote> {
    return safeCall {
        delete {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch (e: IOException) {
        // network unreachable, DNS failure, connection reset — all common on mobile
        return Result.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: SerializationException) {
        return Result.Failure(DataError.Remote.SERIALIZATION_ERROR)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        return Result.Failure(DataError.Remote.UNKNOWN)
    }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Remote> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        401 -> Result.Failure(DataError.Remote.UNAUTHORIZED)
        408 -> Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
        409 -> Result.Failure(DataError.Remote.CONFLICT)
        413 -> Result.Failure(DataError.Remote.PAYLOAD_TOO_LARGE)
        429 -> Result.Failure(DataError.Remote.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Failure(DataError.Remote.SERVER_ERROR)
        else -> Result.Failure(DataError.Remote.UNKNOWN)
    }
}

fun constructRoute(route: String): String {
    return when {
        route.contains(BuildKonfig.BASE_URL) -> route
        route.startsWith("/") -> BuildKonfig.BASE_URL + route
        else -> BuildKonfig.BASE_URL + "/$route"
    }
}
```

`BuildKonfig` (from `com.codingfeline.buildkonfig`) is the KMP equivalent of Android's `BuildConfig` — it generates a `BuildKonfig` Kotlin object into `commonMain` with values sourced from `local.properties` or Gradle.

If you need finer-grained platform exception handling (e.g., distinguishing iOS `NSURLErrorNotConnectedToInternet` from generic `IOException`), add an `expect fun` in `core:data/commonMain` and actualize it per platform — but only when the generic `IOException` catch isn't precise enough.

Usage in a data source stays clean:
```kotlin
suspend fun getNotes(): Result<List<NoteDto>, DataError.Remote> {
    return httpClient.get(route = "/notes")
}
```

---

## Mapping Errors to UiText

Every error type that is displayed to the user should have a `.toUiText()` extension function. Place it in:

- **Feature's `presentation` module** — if the error is feature-specific (e.g., `AuthError.toUiText()`)
- **`core:presentation`** — if the error is shared across features (e.g., `DataError.toUiText()`)

On KMP, `UiText.Resource` wraps a `org.jetbrains.compose.resources.StringResource`, not an Android `R.string` int — see the **kmp-presentation-mvi** skill for the full `UiText` definition.

```kotlin
// core:presentation/commonMain
fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Remote.NO_INTERNET -> UiText.Resource(Res.string.error_no_internet)
        DataError.Remote.SERVER_ERROR -> UiText.Resource(Res.string.error_server)
        DataError.Remote.UNAUTHORIZED -> UiText.Resource(Res.string.error_unauthorized)
        DataError.Local.DISK_FULL -> UiText.Resource(Res.string.error_disk_full)
        // ... map all user-facing cases
        else -> UiText.Resource(Res.string.error_unknown)
    }
}
```

---

## When to Use What

| Scenario | Error type | Example return |
|---|---|---|
| Network call | `DataError.Remote` | `Result<List<NoteDto>, DataError.Remote>` |
| Local DB access | `DataError.Local` | `Result<Note, DataError.Local>` |
| Repository (multi-source) | `DataError` (supertype) | `Result<List<Note>, DataError>` |
| Domain validation | Custom `Error` enum | `EmptyResult<PasswordValidationError>` |
| Auth logic | Custom `Error` enum | `Result<User, AuthError>` |

The `Result` wrapper is not limited to the data layer — use it anywhere a function has typed success and failure outcomes.
