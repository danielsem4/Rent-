---
name: kmp-navigation
description: |
  Type-safe Compose Multiplatform Navigation for Kotlin Multiplatform (Android + iOS) - route objects, feature nav graphs, cross-feature callbacks, and wiring in the shared app module. Use this skill whenever setting up navigation, defining routes, adding a new screen to a nav graph, navigating between features, or wiring nav graphs in the shared app module. Trigger on phrases like "set up navigation", "add a route", "navigate between screens", "nav graph", "NavController", "type-safe nav", "cross-feature navigation", or "NavGraphBuilder".
---

# KMP / Compose Multiplatform Navigation

Jetpack Navigation-Compose is multiplatform as of Nav 2.8 / Compose Multiplatform 1.7. The same `NavHost`, `NavController`, `composable<Route>`, and `navigation<Graph>` APIs work on both Android and iOS from `commonMain`.

## Principles

- **Type-safe navigation** with `@Serializable` route objects (KotlinX Serialization).
- **One nav graph per feature**, defined in the feature's `presentation` module (`commonMain`).
- Feature nav graphs are assembled in `:shared` — the single shared entry point consumed by both Android and iOS.
- Navigation **within** a feature uses a `NavController` passed into the feature nav graph.
- Feature-to-feature navigation uses **callbacks**, keeping features decoupled.

---

## Route Objects (GraphRoutes Pattern)

Routes are defined as nested `@Serializable` objects/data classes inside a `GraphRoutes` interface, in the feature's `presentation/commonMain`:

```kotlin
// feature:notes:presentation/commonMain/navigation/NotesGraphRoutes.kt
interface NotesGraphRoutes {
    @Serializable
    data object Graph : NotesGraphRoutes

    @Serializable
    data object NoteList : NotesGraphRoutes

    @Serializable
    data class NoteDetail(val noteId: String) : NotesGraphRoutes
}
```

Use `data object` for screens with no parameters, `data class` for screens with arguments. The `Graph` object serves as the nav graph marker.

---

## Feature Nav Graph

Each feature exposes a `NavGraphBuilder` extension function in `commonMain`:

```kotlin
// feature:notes:presentation/commonMain/navigation/NotesGraph.kt
fun NavGraphBuilder.notesGraph(
    navController: NavController,
    onNavigateToEditor: (String) -> Unit  // callback for cross-feature navigation
) {
    navigation<NotesGraphRoutes.Graph>(startDestination = NotesGraphRoutes.NoteList) {
        composable<NotesGraphRoutes.NoteList> {
            NoteListRoot(
                onNavigateToDetail = { navController.navigate(NotesGraphRoutes.NoteDetail(it)) }
            )
        }
        composable<NotesGraphRoutes.NoteDetail> { backStackEntry ->
            val route: NotesGraphRoutes.NoteDetail = backStackEntry.toRoute()
            NoteDetailRoot(
                noteId = route.noteId,
                onNavigateToEditor = onNavigateToEditor
            )
        }
    }
}
```

---

## Wiring in `:shared`

All feature nav graphs are assembled in the shared `App()` composable, which Android and iOS both host:

```kotlin
// shared/commonMain — the single shared entry point
@Composable
fun App() {
    AppTheme {
        val navController = rememberNavController()
        NavHost(navController, startDestination = NotesGraphRoutes.Graph) {
            notesGraph(
                navController = navController,
                onNavigateToEditor = { navController.navigate(EditorGraphRoutes.Editor(it)) }
            )
            editorGraph(navController)
        }
    }
}
```

Cross-feature navigation is always expressed as a lambda callback — never by importing a route from another feature module.

### Platform hosts

```kotlin
// shared/androidMain — called from MainActivity
// (App() is invoked inside setContent { App() })

// shared/iosMain
fun MainViewController() = ComposeUIViewController { App() }
```

---

## Passing Arguments

For simple scalar arguments, use `@Serializable data class` routes inside the GraphRoutes interface:

```kotlin
// Inside NotesGraphRoutes interface:
@Serializable
data class NoteDetail(val noteId: String) : NotesGraphRoutes

// Navigate
navController.navigate(NotesGraphRoutes.NoteDetail(noteId = "abc123"))

// Receive
composable<NotesGraphRoutes.NoteDetail> { backStackEntry ->
    val route: NotesGraphRoutes.NoteDetail = backStackEntry.toRoute()
    // route.noteId available here
}
```

Avoid passing complex objects via navigation — pass IDs and load data in the destination ViewModel.

---

## Deep Links

Android deep links work through `deepLinks = listOf(navDeepLink { ... })` on each `composable<Route>`. On iOS, deep-link handling happens at the Swift side (`onOpenURL` / `UIApplicationDelegate`) and calls into a Kotlin bridge that invokes `navController.navigate(...)`. Expose a small `expect` in `commonMain` if you need a uniform API:

```kotlin
// commonMain
expect fun handleDeepLink(uri: String, navController: NavController)
```

Most apps don't need this until they actually ship deep linking — skip the `expect` until required.

---

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| Routes interface | `<Feature>GraphRoutes` | `NotesGraphRoutes`, `EvaluationGraphRoutes` |
| Graph marker | `<Feature>GraphRoutes.Graph` | `NotesGraphRoutes.Graph` |
| Screen route | `<Feature>GraphRoutes.<Screen>` | `NotesGraphRoutes.NoteList` |
| Feature nav graph fn | `<feature>Graph(...)` on `NavGraphBuilder` | `notesGraph(...)` |

---

## Checklist: Adding Navigation to a New Feature

- [ ] Define `<Feature>GraphRoutes` interface with `@Serializable` route objects for each screen in `feature:presentation/commonMain`
- [ ] Add feature nav graph function (`NavGraphBuilder.<feature>Graph(...)`) in `commonMain`
- [ ] Pass `NavController` for intra-feature navigation
- [ ] Expose cross-feature destinations as lambda callbacks (not direct route imports)
- [ ] Wire nav graph and cross-feature callbacks in `:shared`'s shared `NavHost`
