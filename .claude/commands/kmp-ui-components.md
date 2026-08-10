---
name: kmp-ui-components
description: Creating reusable UI components following Rent design system conventions (no-prefix naming, responsive sizing, theme colors)
---

# Creating UI Components in Rent

Use this skill when creating a component, widget, or reusable UI element.

> **Rent note:** Rent has no design system yet — no `core:designsystem` module and no components.
> This skill defines the *conventions* to follow as you build one. Create components under a
> `core:designsystem` module (or, while single-module, an `org.example.rent.core.designsystem`
> package inside `:shared`).

## Naming Convention (no prefix)

Rent's design system uses **no vanity prefix**. Name a component after what it is:

- Give it a **plain descriptive name** (`BarChart`, `LineChart`, `SearchTextField`, `RadioButtonGroup`, `DropDown`, `ToastMessage`).
- Use an **`App` prefix only when the plain name would clash** with a Material3 / Compose type — e.g. `AppButton`, `AppCard`, `AppTextField`, `AppSurface`, `AppDialog`, `AppScaffold`, `AppBaseScreen`, `AppTheme`.
- For cohesion, a family whose base needs the prefix may share it (`AppTextField`, `AppPasswordTextField`) — but don't add `App` to names that already read clearly on their own.

## Where to Place Components

**Shared across features** -> `core/designsystem/src/commonMain/kotlin/org/example/rent/core/designsystem/components/<category>/`

Suggested category folders (create as needed):
- `buttons/` — e.g. `AppButton`, `AppIconButton`, `AppToggleButton`
- `cards/` — e.g. `AppCard`, `IconCard`, `ParagraphCard`
- `charts/` — e.g. `BarChart`, `LineChart`
- `dialogs/` — e.g. `AppDialog`, `ConfirmationDialog`, `DatePickerDialog`
- `layouts/` — e.g. `AppBaseScreen`, `AdaptiveFormLayout`, `AppSurface`
- `textFields/` — e.g. `AppTextField`, `AppPasswordTextField`, `SearchTextField`
- `select/` — e.g. `CheckBoxGroup`, `DropDown`, `RadioButtonGroup`
- `toast/` — e.g. `ToastMessage`, `ToastView`

**Feature-specific** -> `feature/<name>/presentation/src/commonMain/kotlin/org/example/rent/<name>/presentation/components/`

## Check Before Creating

Before creating a new component, check whether an existing one can be used or extended (a button with a `style` enum, a card variant, a text-field variant, a shared dialog, etc.). Prefer extending a design-system component over introducing a near-duplicate.

## Component Template

```kotlin
package org.example.rent.core.designsystem.components.<category>
// OR: package org.example.rent.<feature>.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.rent.core.designsystem.theme.AppTheme
import org.example.rent.core.designsystem.theme.extended
import org.example.rent.core.presentation.util.DeviceConfiguration
import org.example.rent.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.ui.tooling.preview.Preview

// Style enum if component has variants
enum class App<Name>Style {
    DEFAULT,
    OUTLINED,
    COMPACT
}

@Composable
fun App<Name>(
    // Required parameters first
    text: String,
    onClick: () -> Unit,
    // Modifier always with default
    modifier: Modifier = Modifier,
    // Style/config with defaults
    style: App<Name>Style = App<Name>Style.DEFAULT,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    // Optional composable slots last
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val configuration = currentDeviceConfiguration()

    // Responsive sizing based on device
    val contentPadding = when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 8.dp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 10.dp
        DeviceConfiguration.TABLET_PORTRAIT -> 12.dp
        DeviceConfiguration.TABLET_LANDSCAPE -> 14.dp
        DeviceConfiguration.DESKTOP -> 16.dp
    }

    val textStyle = when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> MaterialTheme.typography.bodyMedium
        DeviceConfiguration.MOBILE_LANDSCAPE -> MaterialTheme.typography.bodyMedium
        DeviceConfiguration.TABLET_PORTRAIT -> MaterialTheme.typography.bodyLarge
        DeviceConfiguration.TABLET_LANDSCAPE -> MaterialTheme.typography.bodyLarge
        DeviceConfiguration.DESKTOP -> MaterialTheme.typography.titleMedium
    }

    // Use theme colors
    val backgroundColor = when (style) {
        App<Name>Style.DEFAULT -> MaterialTheme.colorScheme.surface
        App<Name>Style.OUTLINED -> MaterialTheme.colorScheme.background
        App<Name>Style.COMPACT -> MaterialTheme.colorScheme.surfaceVariant
    }

    // Extended colors for custom design tokens
    val borderColor = MaterialTheme.colorScheme.extended.surfaceOutline
    val disabledColor = MaterialTheme.colorScheme.extended.disabledFill
    val secondaryText = MaterialTheme.colorScheme.extended.textSecondary

    // Component implementation
    Card(
        modifier = modifier.padding(contentPadding),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        // ...
    }
}

// Preview at bottom of file
@Composable
@Preview
fun App<Name>Preview() {
    AppTheme {
        App<Name>(
            text = "Preview",
            onClick = {}
        )
    }
}

@Composable
@Preview
fun App<Name>DarkPreview() {
    AppTheme(darkTheme = true) {
        App<Name>(
            text = "Dark Preview",
            onClick = {}
        )
    }
}
```

## Theme & Color Usage

**Standard Material colors:**
```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onPrimary
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.error
```

**Extended colors** (import `org.example.rent.core.designsystem.theme.extended`) — define an
`ExtendedColors` holder exposed via a `CompositionLocal` for design tokens Material3 doesn't cover:
```kotlin
MaterialTheme.colorScheme.extended.primaryHover
MaterialTheme.colorScheme.extended.textPrimary
MaterialTheme.colorScheme.extended.textSecondary
MaterialTheme.colorScheme.extended.textPlaceholder
MaterialTheme.colorScheme.extended.textDisabled
MaterialTheme.colorScheme.extended.disabledFill
MaterialTheme.colorScheme.extended.disabledOutline
MaterialTheme.colorScheme.extended.surfaceLower
MaterialTheme.colorScheme.extended.surfaceHigher
MaterialTheme.colorScheme.extended.surfaceOutline
MaterialTheme.colorScheme.extended.success
MaterialTheme.colorScheme.extended.onSuccess
// Accent colors: accentBlue, accentPurple, accentViolet, accentPink, accentOrange, etc.
```

## Responsive Design Pattern

Always use `currentDeviceConfiguration()` for responsive sizing:

```kotlin
val configuration = currentDeviceConfiguration()

// Branch on device type for sizing
val padding = when (configuration) {
    DeviceConfiguration.MOBILE_PORTRAIT -> 8.dp
    DeviceConfiguration.MOBILE_LANDSCAPE -> 10.dp
    DeviceConfiguration.TABLET_PORTRAIT -> 12.dp
    DeviceConfiguration.TABLET_LANDSCAPE -> 14.dp
    DeviceConfiguration.DESKTOP -> 16.dp
}
```

The `DeviceConfiguration` enum values are:
- `MOBILE_PORTRAIT` — narrow phone, portrait
- `MOBILE_LANDSCAPE` — phone in landscape
- `TABLET_PORTRAIT` — tablet portrait
- `TABLET_LANDSCAPE` — tablet landscape
- `DESKTOP` — desktop/large window

## Naming Conventions

- Shared components: `<Name>.kt` file, `<Name>` composable — `App` prefix only on Material/Compose clashes (see **Naming Convention** above)
- Style enums: `<Name>Style` with UPPER_CASE values
- Feature components: descriptive names in the feature's `components/` package (e.g., `ListingSection`, `MessageSection`)
- Previews: `@Composable @Preview fun <Name>Preview()` at bottom of file
- Wrap previews in `AppTheme { }`, add a dark variant with `AppTheme(darkTheme = true)`

## API Design Pattern

Follow this parameter ordering convention:
1. Required data parameters (text, items, values)
2. Required callbacks (onClick, onValueChange)
3. `modifier: Modifier = Modifier`
4. Style/configuration with defaults (style, enabled, isLoading)
5. Optional composable slot lambdas (leadingIcon, trailingContent)
