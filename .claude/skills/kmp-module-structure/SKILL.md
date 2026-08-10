---
name: kmp-module-structure
description: Module layout, dependency rules, Gradle convention plugins, and source-set structure for Kotlin Multiplatform projects targeting Android and iOS with Compose Multiplatform. Use this skill whenever setting up a new KMP/CMP project, deciding where a new module should live, asking "how should I structure this", creating a new feature module, adding a core submodule, configuring Gradle convention plugins, working with version catalogs, wiring `expect`/`actual`, or making any decision about project-level architecture. Trigger on phrases like "set up the project", "add a module", "create a feature", "how should I structure", "project structure", "convention plugin", "build-logic", "source set", "commonMain", "expect/actual", or "where does X live".
---

# KMP / Compose Multiplatform Module Structure

> **Where Rent is today:** Rent currently ships as the JetBrains KMP wizard template — a single
> `:shared` module (+ `:androidApp` + `iosApp`), package root `org.example.rent`, no `build-logic`
> / convention plugins, no DI / networking / navigation yet. This skill describes the **target
> structure to grow into** as features are added. When Rent is still single-module, treat the
> `:feature:*` / `:core:*` layers below as *packages inside `:shared`* first, then split them into
> real Gradle modules once a layer earns its own build config.

## Core Philosophy

- **Feature-layered modularization**: split by feature first, then by layer within each feature.
- **Clean Architecture layers**: `presentation` → `domain` ← `data`. Domain is innermost and depends on nothing.
- **Code lives in a feature module unless it is needed by more than one feature** — then it moves to the appropriate `core` submodule.
- Features **never depend on each other**. Cross-feature shared data belongs in `core:domain` (domain models) or `core:presentation` (shared composables/UI logic), not in the owning feature.
- **Common-first**: write every module as a `kotlin-multiplatform` module with `commonMain` as the primary source set. Drop into `androidMain` / `iosMain` only for platform-specific APIs via `expect`/`actual`.

---

## Module Layout

```
:shared                         ← Shared Compose app entry point (iOS + Android wire into this)
:androidApp                     ← Android Application + MainActivity (thin)
:iosApp                         ← Xcode project (consumes :shared as a framework)
:build-logic                    ← Gradle convention plugins
:core:domain                    ← Pure Kotlin — models, repo interfaces, error types, Result
:core:data                      ← Ktor HttpClient factory, shared DB, safe-call helpers
:core:presentation              ← Shared UI utilities (ObserveAsEvents, UiText, etc.)
:core:designsystem              ← Reusable Compose components, colors, theme, typography
:feature:<name>:domain          ← Feature-specific domain models, repo interfaces, error types
:feature:<name>:data            ← Repo implementations, DTOs, mappers, Room DAOs
:feature:<name>:presentation    ← ViewModel, screen composables, state, actions, events
```

The `:shared` module holds the shared root composable (`App()`), navigation host, and Koin initialization. Android's `MainActivity` and the iOS `MainViewController` each call into `App()`.

For standalone, self-contained concerns that involve meaningful complexity (multiple classes, configuration, or a non-trivial API surface), create a dedicated module under `:core` (e.g., `:core:location`, `:core:analytics`). Do not create a separate module for a single class or a trivial utility — that belongs in an existing `core` module instead.

A shared Room database is a good candidate for a dedicated `:core:database` module. It contains the `@Database` class, all entity definitions, all DAOs, and migrations. Room KMP currently supports Android, iOS, JVM, and native targets; it does **not** support Web/Wasm. Feature modules that need DB access depend on `:core:database` directly, while features that don't need it remain decoupled.

---

## Source Set Structure

Every KMP module uses the standard hierarchical source set layout:

```
feature/notes/presentation/
└── src/
    ├── commonMain/kotlin/     ← shared code (>95% of content lives here)
    ├── androidMain/kotlin/    ← Android-only actuals and extensions
    ├── iosMain/kotlin/        ← iOS-only actuals and extensions
    ├── commonTest/kotlin/     ← shared tests (kotlin.test)
    ├── androidUnitTest/       ← Android-specific tests (JUnit5 optional here)
    └── iosTest/               ← iOS-specific tests
```

Rule of thumb: **default to `commonMain`**. Push code into platform source sets only when the API is genuinely platform-specific and cannot be expressed via a multiplatform library. Use `expect` declarations in `commonMain` and `actual` implementations in the platform source sets.

---

## Dependency Rules

| Layer | May depend on |
|---|---|
| `presentation` | `domain` (own feature), `core:domain`, `core:presentation`, `core:designsystem` |
| `data` | `domain` (own feature), `core:domain`, `core:data` |
| `domain` | `core:domain` only — never `data` or `presentation` |
| `:shared` | all feature + core modules (wires everything) |
| `:androidApp` / `:iosApp` | `:shared` + any platform-specific bootstrap |

**Every** layer and module may access `core:domain`. Domain and lower layers stay in `commonMain` almost exclusively.

---

## Convention Plugins (`:build-logic`)

Define a convention plugin for every non-trivial Gradle config:

| Plugin | Purpose |
|---|---|
| `convention.cmp.library` | Base KMP library with Compose: targets Android + iOS, sets JVM toolchain, wires common deps |
| `convention.cmp.feature` | `convention.cmp.library` + Koin + shared feature deps bundled (for data + presentation layers) |
| `convention.kmp.library` | Non-Compose KMP library (for domain layers — pure Kotlin) |
| `convention.cmp.application` | Main app module (`:shared`) |
| `convention.room` | Room database setup |
| `convention.buildkonfig` | Build-time config (BuildKonfig) |

Use **version catalogs** (`gradle/libs.versions.toml`) for all dependency and version management. No hardcoded versions in build files. Rent's catalog is already the single source of versions — extend it there rather than inlining versions.

> **Toolchain note (Rent):** Rent uses the modern AGP KMP library plugin
> (`com.android.kotlin.multiplatform.library`) with the `kotlin { android { } }` DSL — not the
> legacy `androidTarget()` + `com.android.library`. Match that DSL in any new module's build file,
> and align dependency versions with the existing catalog (Kotlin 2.4.10, Compose Multiplatform
> 1.11.1) rather than introducing conflicting versions.

---

## Expect / Actual

Reach for `expect`/`actual` only when a multiplatform library does not already cover the API. Common cases:

```kotlin
// commonMain
expect class PlatformContext

expect fun openUrl(url: String)

expect fun randomUUID(): String
```

```kotlin
// androidMain
actual typealias PlatformContext = android.content.Context

actual fun randomUUID(): String = java.util.UUID.randomUUID().toString()
```

```kotlin
// iosMain
import platform.Foundation.NSUUID

actual class PlatformContext  // no-op placeholder on iOS

actual fun randomUUID(): String = NSUUID().UUIDString
```

Keep `expect` surfaces small and shallow — the goal is a thin platform bridge, not a duplicated implementation.

---

## Key Libraries

| Concern | Library | Notes |
|---|---|---|
| DI | Koin (+ `koin-compose-viewmodel`) | Multiplatform |
| Networking | Ktor Client | Engines: `OkHttp` (Android), `Darwin` (iOS) |
| Local DB | Room (KMP) | Android + iOS supported; needs KSP per target |
| Preferences | DataStore (multiplatform) | `preferencesDataStoreFile` wired per platform |
| Navigation | Jetpack Navigation-Compose | Multiplatform since Nav 2.8 / Compose 1.7 |
| Serialization | KotlinX Serialization | Ktor + Nav routes |
| Image loading | Coil 3 | Multiplatform |
| Logging | Kermit | Multiplatform |
| Async | Coroutines + Flow | Multiplatform |
| Date/Time | `kotlinx-datetime` | Multiplatform |
| Background tasks | Platform-specific | `WorkManager` on Android; `BGTaskScheduler` on iOS — wrap via `expect` |
| Secrets | `BuildKonfig` | Replaces Android `BuildConfig` across targets |
| Testing | `kotlin.test`, Turbine, AssertK, `kotlinx-coroutines-test` | All multiplatform |
| UI testing | `runComposeUiTest { }` | JetBrains Compose test API |

> Most of the libraries above are **not yet in Rent's catalog** (only Compose + lifecycle are). Add
> them to `gradle/libs.versions.toml` as each feature needs them — don't pull versions from memory.

---

## Platform Entry Points

### Android (`:androidApp`)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MyApp)
        }
    }
}
```

### iOS (`:iosApp`)

```kotlin
// shared/src/iosMain/kotlin/MainViewController.kt
fun MainViewController() = ComposeUIViewController { App() }

fun initKoinIos() = initKoin()  // called from Swift on app start
```

```swift
// iosApp/iOSApp.swift
@main
struct iOSApp: App {
    init() { KoinKt.initKoinIos() }
    var body: some Scene {
        WindowGroup { ComposeView() }
    }
}
```

---

## Checklist: Adding a New Feature Module

- [ ] Create `:feature:<name>:domain`, `:feature:<name>:data`, `:feature:<name>:presentation`
- [ ] Apply appropriate convention plugins (`domain-module`, `kmp-library`/`kmp-feature`)
- [ ] Write code in `commonMain` by default
- [ ] Add `androidMain`/`iosMain` source sets only for platform-specific actuals
- [ ] Verify no cross-feature dependencies are introduced
- [ ] Wire the feature into `:shared` (nav graph + Koin modules)
- [ ] If logic is shared across 2+ features, extract to the appropriate `core` submodule
