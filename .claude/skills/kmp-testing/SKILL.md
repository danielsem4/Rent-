---
name: kmp-testing
description: |
  Testing patterns for Kotlin Multiplatform (Android + iOS) - ViewModel unit tests with kotlin.test (commonTest), Turbine, AssertK, UnconfinedTestDispatcher, fake repositories, SavedStateHandle, and Compose UI tests with runComposeUiTest. Use this skill whenever writing or reviewing tests for ViewModels, repositories, use cases, or Compose screens on KMP. Trigger on phrases like "write a test", "unit test the ViewModel", "test a repository", "Turbine", "fake repository", "UnconfinedTestDispatcher", "runTest", "runComposeUiTest", "kotlin.test", "JUnit5", or "ComposeTestRule".
---

# KMP / Compose Multiplatform Testing

## Stack

| Concern | Library | Source set |
|---|---|---|
| Test framework (common) | `kotlin.test` (`@Test`, `assertEquals`, `assertTrue`) | `commonTest` |
| Test framework (JVM only) | JUnit5 | `androidUnitTest` / `jvmTest` only |
| Assertions | AssertK | `commonTest` |
| Flow / StateFlow testing | Turbine | `commonTest` |
| Coroutine testing | `kotlinx-coroutines-test` + `UnconfinedTestDispatcher` | `commonTest` |
| Compose UI testing | `runComposeUiTest { }` (JetBrains) | `commonTest` |

> **Rent note:** only `kotlin.test` is currently on the classpath (no test source sets exist yet).
> Add Turbine, AssertK, and `kotlinx-coroutines-test` to `gradle/libs.versions.toml` and create the
> `commonTest` source set before using the patterns below.

**Rule:** put shared tests (ViewModel logic, repository logic, mappers) in `commonTest`. Use JVM-only test frameworks (JUnit5, Robolectric) only in `androidUnitTest` when the test genuinely needs Android framework APIs.

---

## ViewModel Unit Tests

### Setup

Use `kotlin.test` annotations in `commonTest` so tests run on Android and iOS:

```kotlin
// commonTest
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class NoteListViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
```

> Note: `@BeforeEach`/`@AfterEach` are JUnit5-only and won't compile in `commonTest`. Use `@BeforeTest`/`@AfterTest` from `kotlin.test`.

### Testing State with Turbine

```kotlin
@Test
fun `loading notes emits notes in state`() = runTest {
    val repo = FakeNoteRepository()
    val viewModel = NoteListViewModel(repo)

    viewModel.state.test {
        viewModel.onAction(NoteListAction.OnRefreshClick)
        assertThat(awaitItem().isLoading).isTrue()
        assertThat(awaitItem().notes).isNotEmpty()
    }
}
```

> Backtick-quoted test names work on Android but are invalid on iOS/Kotlin/Native. If you share the test in `commonTest`, use camelCase or underscore naming:
>
> ```kotlin
> @Test
> fun loadingNotesEmitsNotesInState() = runTest { ... }
> ```

### Testing Events (one-time side effects)

```kotlin
@Test
fun clickingNoteSendsNavigateToDetailEvent() = runTest {
    val viewModel = NoteListViewModel(FakeNoteRepository())

    viewModel.events.test {
        viewModel.onAction(NoteListAction.OnNoteClick("123"))
        assertThat(awaitItem()).isEqualTo(NoteListEvent.NavigateToDetail("123"))
    }
}
```

---

## Fake Repositories

Prefer **fakes** (not mocks) for repository dependencies. Mocking libraries like MockK are JVM-only; fakes are multiplatform and usually simpler. A fake is an in-memory implementation:

```kotlin
// commonTest
class FakeNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    var shouldReturnError = false

    override suspend fun getNotes(): Result<List<Note>, DataError.Local> {
        return if (shouldReturnError) Result.Failure(DataError.Local.UNKNOWN)
        else Result.Success(notes.toList())
    }

    override suspend fun insertNote(note: Note): EmptyResult<DataError.Local> {
        notes.add(note)
        return Result.Success(Unit)
    }
}
```

---

## SavedStateHandle in Tests

Instantiate it directly — no mocking needed. Works identically on Android and iOS with the multiplatform lifecycle artifact:

```kotlin
val savedStateHandle = SavedStateHandle(mapOf("noteId" to "123"))
val viewModel = NoteEditorViewModel(savedStateHandle, FakeNoteRepository())
```

---

## When to Inject Dispatchers

Only inject `CoroutineDispatcher` into a class when:
1. It dispatches to a non-main dispatcher (e.g., `IO`), AND
2. That class is directly unit-tested.

ViewModels that only use `viewModelScope` do not need injected dispatchers. Use `Dispatchers.setMain()` in tests instead.

If a non-ViewModel class uses `withContext(Dispatchers.IO)` and is unit-tested, inject the dispatcher:

```kotlin
class ImageCompressor(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun compress(bytes: ByteArray): ByteArray = withContext(ioDispatcher) { ... }
}

// In test:
val compressor = ImageCompressor(ioDispatcher = UnconfinedTestDispatcher())
```

---

## Compose UI Tests

Compose Multiplatform ships `runComposeUiTest { }` in `commonTest` (provided by `org.jetbrains.compose.ui:ui-test`). It replaces Android's `ComposeTestRule`/`createComposeRule` but has a nearly identical API:

```kotlin
// commonTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

class NoteListScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun displaysNotesAfterLoad() = runComposeUiTest {
        setContent {
            NoteListScreen(
                state = NoteListState(notes = listOf(NoteUi("1", "Hello", "Mar 15"))),
                onAction = {}
            )
        }
        onNodeWithText("Hello").assertIsDisplayed()
    }
}
```

On Android, Compose UI tests can still be run as instrumented tests via `createAndroidComposeRule<ComponentActivity>()` in `androidInstrumentedTest` when they need a real `Activity` context. On iOS, `runComposeUiTest` runs headless via a Skia renderer.

---

## Robot Pattern (Complex UI / E2E Tests)

For complex end-to-end or multi-step UI tests, use the **Robot Pattern** to separate test intent from Compose interactions. A robot encapsulates all UI interactions for a screen, keeping tests readable and DRY.

### Structure

Robots accept a `ComposeUiTest` receiver (multiplatform) instead of `ComposeContentTestRule`. Every robot function returns `this` so calls can be chained:

```kotlin
// commonTest
@OptIn(ExperimentalTestApi::class)
class NoteListRobot(private val test: ComposeUiTest) {

    fun setContent(
        state: NoteListState,
        onAction: (NoteListAction) -> Unit = {}
    ) = apply {
        test.setContent {
            NoteListScreen(state = state, onAction = onAction)
        }
    }

    fun assertNoteVisible(title: String) = apply {
        test.onNodeWithText(title).assertIsDisplayed()
    }

    fun clickNote(title: String) = apply {
        test.onNodeWithText(title).performClick()
    }

    fun assertEmptyState() = apply {
        test.onNodeWithTag("empty_state").assertIsDisplayed()
    }
}
```

### Usage in Tests

```kotlin
@OptIn(ExperimentalTestApi::class)
class NoteListScreenTest {

    @Test
    fun displaysNotesAfterLoad() = runComposeUiTest {
        NoteListRobot(this)
            .setContent(NoteListState(notes = listOf(NoteUi("1", "Hello", "Mar 15"))))
            .assertNoteVisible("Hello")
    }

    @Test
    fun showsEmptyStateWhenNoNotes() = runComposeUiTest {
        NoteListRobot(this)
            .setContent(NoteListState(notes = emptyList()))
            .assertEmptyState()
    }

    @Test
    fun clickingNoteTriggersAction() = runComposeUiTest {
        var clickedId: String? = null
        NoteListRobot(this)
            .setContent(
                state = NoteListState(notes = listOf(NoteUi("1", "Hello", "Mar 15"))),
                onAction = { if (it is NoteListAction.OnNoteClick) clickedId = it.noteId }
            )
            .assertNoteVisible("Hello")
            .clickNote("Hello")
    }
}
```

**When to use:** Apply the robot pattern when a screen has 3+ UI test cases, when multiple tests share the same setup/assertion sequences, or when testing complex multi-step user flows (e.g., fill form → submit → assert result).

---

## What to Test

- Unit-test every ViewModel and any non-trivial domain/data logic — place these in `commonTest` so they run on both platforms.
- Unit-test any logic that is likely to change.
- Use fakes over mocks where possible — fakes are simpler, multiplatform, and catch more real bugs than mock-based tests.
- Write integration tests where DB/network interactions are non-trivial. Use Ktor's `MockEngine` for network and Room's in-memory DB (Android) or a temp file path (iOS) for DB tests.
- Write E2E Compose tests with `runComposeUiTest` for critical user flows.
- Use the robot pattern for complex UI/E2E tests with multiple test cases or shared interaction sequences.
- Keep Android-specific instrumented tests (Activity, Context-dependent behavior) in `androidInstrumentedTest`, not `commonTest`.
