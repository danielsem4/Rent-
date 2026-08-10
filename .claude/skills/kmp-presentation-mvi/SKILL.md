---
name: kmp-presentation-mvi
description: |
  MVI presentation layer for Kotlin Multiplatform + Compose Multiplatform (Android + iOS) - State, Action, Event, multiplatform ViewModel, Root/Screen composable split, UI models, UiText error mapping with compose.resources, and process death with SavedStateHandle. Use this skill whenever creating or reviewing a ViewModel, defining screen state, actions, or events, structuring composables, mapping errors to UI strings, or handling process death across platforms. Trigger on phrases like "add a ViewModel", "create a screen", "MVI", "state", "action", "event", "screen composable", "UiText", "SavedStateHandle", "ObserveAsEvents", "UI model", or "compose resources".
---

# KMP / Compose Multiplatform Presentation Layer (MVI)

## Overview

Every screen has:
1. **State** — a single data class holding all UI state fields.
2. **Action** (Intent) — a sealed interface of all user-triggered actions.
3. **Event** — a sealed interface of one-time side effects (navigation, snackbar).
4. **ViewModel** — holds `StateFlow<State>`, processes `Action`, emits `Event` via `Channel`.

All of this lives in `commonMain`. The `androidx.lifecycle` ViewModel, `SavedStateHandle`, `viewModelScope`, and `collectAsStateWithLifecycle` are all multiplatform since lifecycle 2.8+.

> **Rent note:** the ViewModel dependencies (`lifecycle-viewmodel-compose`,
> `lifecycle-runtime-compose`) are already on Rent's classpath in `shared/commonMain`, so you can
> introduce ViewModels without adding dependencies. `koinViewModel()` requires adding Koin — see
> the **kmp-di-koin** skill.

### Required multiplatform artifacts

- `androidx.lifecycle:lifecycle-viewmodel` — multiplatform `ViewModel` base class
- `androidx.lifecycle:lifecycle-viewmodel-savedstate` — `SavedStateHandle`
- `androidx.lifecycle:lifecycle-viewmodel-compose` — `koinViewModel()` binding via `koin-compose-viewmodel`
- `androidx.lifecycle:lifecycle-runtime-compose` — `collectAsStateWithLifecycle`

---

## State

```kotlin
// commonMain
data class NoteListState(
    val notes: List<NoteUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null
)
```

Always update state with `.update { }` — never replace the entire flow:
```kotlin
_state.update { it.copy(isLoading = true) }
```

---

## Action (Intent)

```kotlin
sealed interface NoteListAction {
    data object OnRefreshClick : NoteListAction
    data class OnNoteClick(val noteId: String) : NoteListAction
    data class OnDeleteNote(val noteId: String) : NoteListAction
}
```

---

## Event (one-time side effects)

```kotlin
sealed interface NoteListEvent {
    data class NavigateToDetail(val noteId: String) : NoteListEvent
    data class ShowSnackbar(val message: UiText) : NoteListEvent
}
```

---

## ViewModel

```kotlin
// commonMain — the multiplatform ViewModel from androidx.lifecycle
class NoteListViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state = _state.asStateFlow()

    private val _events = Channel<NoteListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: NoteListAction) {
        when (action) {
            is NoteListAction.OnRefreshClick -> loadNotes()
            is NoteListAction.OnNoteClick -> {
                viewModelScope.launch {
                    _events.send(NoteListEvent.NavigateToDetail(action.noteId))
                }
            }
            else -> Unit
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            noteRepository.getNotes()
                .onSuccess { notes ->
                    _state.update { it.copy(notes = notes.map { it.toNoteUi() }, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(NoteListEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }
}
```

`viewModelScope` and `Dispatchers.Main` are both available across Android and iOS — no `expect`/`actual` needed.

---

## Coroutine Dispatchers

**Do not inject** unless the class is unit-tested and dispatches to a non-main dispatcher. For ViewModel tests, use `Dispatchers.setMain(UnconfinedTestDispatcher())` in test setup.

For blocking code that doesn't support suspension, wrap it:
```kotlin
suspend fun compressImage(bytes: ByteArray): ByteArray = withContext(Dispatchers.IO) {
    // blocking compression logic
}
```

`Dispatchers.IO` is available on Kotlin/Native (iOS) since coroutines 1.7+.

Only inject `CoroutineDispatcher` when:
1. The class dispatches to a non-main dispatcher (e.g., `IO`), AND
2. That class is directly unit-tested.

---

## Mapping Errors to UiText

`UiText` (`core:presentation`, `commonMain`) wraps strings that originate from — or could originate from — a Compose Multiplatform string resource:

```kotlin
import org.jetbrains.compose.resources.StringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class Resource(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ) : UiText

    @Composable
    fun asString(): String

    suspend fun asStringAsync(): String
}
```

**Resolving `UiText` in a composable:**

```kotlin
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.Resource -> stringResource(resource = id, *args)
}
```

**When to use `UiText`:** For any string that comes from a `Res.string.*`, could be localized, or might be either a resource or a dynamic value depending on context (e.g., error messages that map to localized strings).

**When to use plain `String`:** For values that are always dynamic and never come from resources — e.g., a user's name, a formatted date, a currency amount. These should be exposed as `String` directly in the state or UI model.

```kotlin
// UiText — error message that maps to a localized resource
data class NoteListState(
    val error: UiText? = null
)

// Plain String — always dynamic, never a resource
data class NoteUi(
    val authorName: String,
    val formattedDate: String
)
```

Define `DataError.toUiText()` extension functions in `core:presentation/commonMain` that map error enums to `UiText.Resource(Res.string.xxx)`. Note the key difference from Android-only code: you wrap a `StringResource`, **not** an `Int` resource id.

---

## UI Model (Presentation Model)

When a domain model needs UI-specific formatting (dates, units, currency), create a dedicated UI model in the presentation layer. Use `kotlinx-datetime` for date formatting in `commonMain`:

```kotlin
// commonMain
data class NoteUi(
    val id: String,
    val title: String,
    val formattedDate: String  // e.g. "Mar 15, 2026"
)

fun Note.toNoteUi(): NoteUi = NoteUi(
    id = id,
    title = title,
    formattedDate = date.toLocalDateTime(TimeZone.currentSystemDefault()).format(...)
)
```

UI models are always suffixed with `Ui` (e.g., `NoteUi`, `TodoItemUi`).

---

## Composable Structure

Both the Root and Screen composable live in the **same file** (e.g., `NoteListScreen.kt`) under `commonMain`.

### Root Composable (suffixed `Root`)

Receives the ViewModel (via `koinViewModel()`) and any callbacks needed for navigation. Observes events. Passes state and `onAction` down.

### Screen Composable (suffixed `Screen`)

Receives only `state` and `onAction`. No ViewModel reference. Can be previewed independently with CMP's `@Preview`.

```kotlin
// commonMain/NoteListScreen.kt — Root + Screen in a single file
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NoteListRoot(
    onNavigateToDetail: (String) -> Unit,
    viewModel: NoteListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteListEvent.NavigateToDetail -> onNavigateToDetail(event.noteId)
            is NoteListEvent.ShowSnackbar -> { /* show snackbar */ }
        }
    }

    NoteListScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun NoteListScreen(
    state: NoteListState,
    onAction: (NoteListAction) -> Unit
) { ... }

@Preview
@Composable
private fun NoteListScreenPreview() {
    NoteListScreen(state = NoteListState(), onAction = {})
}
```

> CMP's `@Preview` only renders on desktop/JVM (via the IDE preview panel). iOS-side previews require the Xcode canvas against the generated framework.

---

## Process Death

`SavedStateHandle` is multiplatform (lifecycle 2.8+). Koin's `viewModelOf` + `koinViewModel()` will inject it automatically on both Android and iOS:

```kotlin
class NoteEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        NoteEditorState(
            title = savedStateHandle["title"] ?: "",
            body = savedStateHandle["body"] ?: ""
        )
    )

    fun onAction(action: NoteEditorAction) {
        when (action) {
            is NoteEditorAction.OnTitleChange -> {
                savedStateHandle["title"] = action.title
                _state.update { it.copy(title = action.title) }
            }
            else -> Unit
        }
    }
}
```

On Android this survives process death through the system `Bundle`. On iOS, state restoration behavior is different — the iOS runtime restores process state via `NSCoder`/state restoration; for Compose Multiplatform on iOS, `SavedStateHandle` survives configuration-change-like events and navigation back-stack pops, not full app termination.

Only save what truly matters — not the entire state.

---

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| ViewModel | `<Screen>ViewModel` | `NoteListViewModel` |
| State | `<Screen>State` | `NoteListState` |
| Action | `<Screen>Action` | `NoteListAction` |
| Event | `<Screen>Event` | `NoteListEvent` |
| Root composable | `<Screen>Root` | `NoteListRoot` |
| Screen composable | `<Screen>Screen` | `NoteListScreen` |
| UI model | `<Model>Ui` | `NoteUi`, `TodoItemUi` |

---

## Checklist: Adding a New Screen

- [ ] Define `State`, `Action`, `Event` in `feature:presentation/commonMain`
- [ ] Implement `ViewModel` in `feature:presentation/commonMain` (extends multiplatform `ViewModel`)
- [ ] Create `<Screen>Root` composable (holds ViewModel via `koinViewModel()`, observes events)
- [ ] Create `<Screen>Screen` composable (pure state + onAction, previewable via CMP `@Preview`)
- [ ] Map any domain errors to `UiText` via extension functions using `Res.string.*`
- [ ] Add `SavedStateHandle` for any form fields that must survive process death
