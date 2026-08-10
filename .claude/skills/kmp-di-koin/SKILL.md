---
name: kmp-di-koin
description: |
  Koin dependency injection setup for Kotlin Multiplatform (Android + iOS) - module definitions per layer, multiplatform ViewModel injection, assembling modules in the shared app module, koinViewModel() in composables, and platform-specific Koin initialization on Android and iOS. Use this skill whenever setting up Koin, defining a DI module, providing a repository or ViewModel, injecting a dependency, or wiring modules at the app entry points. Trigger on phrases like "set up Koin", "add a Koin module", "inject a dependency", "DI module", "koinViewModel", "provide a ViewModel", "startKoin", "KoinApplication", "initKoin", or "single/viewModel/factory".
---

# KMP / Compose Multiplatform Dependency Injection (Koin)

Koin is multiplatform. On KMP you get the same `module { }` DSL in `commonMain`, plus `koin-compose-viewmodel` for binding ViewModels into composables.

> **Rent note:** Koin is **not yet in Rent's catalog** — add the artifacts below to
> `gradle/libs.versions.toml` before wiring DI. Until modules are split out, the `initKoin` helper
> and all modules live in `:shared`.

## Principles

- One Koin module per feature layer — create it only if there are dependencies to provide.
- Feature modules live in `commonMain` of the feature module they belong to.
- Modules are assembled in the shared `:shared` module (or an `initKoin` helper), **never** in feature modules themselves.
- In Compose root composables, always inject ViewModels via `koinViewModel()` from `org.koin.compose.viewmodel`.
- Platform-specific bindings (Android `Context`, iOS file paths, etc.) go in platform-specific modules and are appended during `initKoin` on each platform.

---

## Required artifacts

- `io.insert-koin:koin-core` — `commonMain`
- `io.insert-koin:koin-compose` — `commonMain` (provides `KoinApplication`, `koinInject`)
- `io.insert-koin:koin-compose-viewmodel` — `commonMain` (provides `koinViewModel()`)
- `io.insert-koin:koin-android` — `androidMain` only (for `androidContext`)

---

## Module Definitions

Prefer the constructor-reference overloads (`singleOf`, `viewModelOf`, `factoryOf`) — they are more concise and let Koin resolve parameters automatically. Only fall back to the lambda overloads (`single { ... }`, `viewModel { ... }`, `factory { ... }`) when constructor injection alone is not enough, e.g. when you need to call a factory method, pass a named/qualified dependency, or do post-construction setup.

### Data layer module

```kotlin
// feature:notes:data/commonMain
val notesDataModule = module {
    singleOf(::OfflineFirstNoteRepository) { bind<NoteRepository>() }
    singleOf(::RoomNoteDataSource) { bind<NoteLocalDataSource>() }
    singleOf(::KtorNoteDataSource) { bind<NoteRemoteDataSource>() }
}
```

### Presentation layer module

```kotlin
// feature:notes:presentation/commonMain
val notesPresentationModule = module {
    viewModelOf(::NoteListViewModel)
    viewModelOf(::NoteDetailViewModel)
}
```

### Core data module — with platform split

`HttpClient` engine and DB file paths vary per platform, so split into a common module (business-logic wiring) and platform modules (actual engines/paths):

```kotlin
// core:data/commonMain
val coreDataModule = module {
    singleOf(::SessionPreferences)
    single { HttpClientFactory.create(get()) } // engine is provided by a platform module
    single { createDataStore(get()) }
    single { DatabaseBuilder(get()).build() }
}

// core:data/androidMain
val platformDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { DatabaseBuilder(androidContext()) }
}

// core:data/iosMain
val platformDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single { DatabaseBuilder() }
}
```

---

## Shared Koin Initialization

Define an `initKoin` function in `:shared` (or a dedicated `:di` module) and let each platform call it:

```kotlin
// shared/commonMain
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            // core
            coreDataModule,
            platformDataModule,
            // features
            notesDataModule,
            notesPresentationModule,
            authDataModule,
            authPresentationModule,
        )
    }
}
```

### Android (`:androidApp`)

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MyApp)
        }
    }
}
```

### iOS (`:shared/iosMain`)

```kotlin
// iosMain
fun initKoinIos() = initKoin()
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

The Android-only `androidContext` binding goes through the `KoinAppDeclaration` passed to `initKoin` — it never leaks into `commonMain`.

---

## Injecting in Composables

Always use `koinViewModel()` in Root composables:

```kotlin
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteListRoot(
    onNavigateToDetail: (String) -> Unit,
    viewModel: NoteListViewModel = koinViewModel()
) { ... }
```

For non-ViewModel dependencies inside composables, use `koinInject()`:

```kotlin
val analytics: Analytics = koinInject()
```

Never pass ViewModels down the composable tree — inject at the Root level only.

---

## Scoping Rules

| Scope | Preferred form | Fallback form | When to use |
|---|---|---|---|
| Singleton | `singleOf(::Impl) { bind<Interface>() }` | `single { ... }` | One instance for the app lifetime (repositories, HttpClient, DB) |
| ViewModel | `viewModelOf(::MyViewModel)` | `viewModel { ... }` | ViewModel instances scoped to their lifecycle |
| Factory | `factoryOf(::Impl)` | `factory { ... }` | New instance on every injection (rare — prefer singleton or ViewModel) |

Use the `*Of` constructor-reference form by default. Only use the lambda form when you cannot express the binding with a constructor reference (factory methods, named qualifiers, manual setup).

---

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| Koin module | `<feature><Layer>Module` | `notesDataModule`, `notesPresentationModule` |
| Platform module | `platform<Layer>Module` | `platformDataModule` |
| Init function | `initKoin` (common) / `initKoinIos` (iOS bridge) | |

---

## Checklist: Adding DI for a New Feature

- [ ] Define `val <feature>DataModule = module { ... }` in `feature:data/commonMain`
- [ ] Define `val <feature>PresentationModule = module { ... }` in `feature:presentation/commonMain`
- [ ] Register both modules in `initKoin`'s `modules(...)` block in `:shared`
- [ ] If the feature needs platform-specific bindings (Context, file paths, Keychain), add a `platform<Feature>Module` in `androidMain` / `iosMain` and include it in `initKoin`
- [ ] Use `koinViewModel()` in all Root composables
