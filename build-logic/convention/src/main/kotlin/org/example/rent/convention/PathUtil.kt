package org.example.rent.convention

import org.gradle.api.Project

/** Root package for every module — derived from the app's namespace (`org.example.rent`). */
private const val PACKAGE_ROOT = "org.example.rent"

/**
 * Derives an Android namespace / package name from the Gradle module path.
 *
 *  - `:shared`              -> `org.example.rent.shared`
 *  - `:feature`             -> `org.example.rent.feature`
 *  - `:core:designsystem`   -> `org.example.rent.core.designsystem`
 */
internal fun pathToPackageName(project: Project): String {
    val suffix = project.path
        .removePrefix(":")
        .replace(':', '.')
        .replace("-", "")
    return if (suffix.isBlank()) PACKAGE_ROOT else "$PACKAGE_ROOT.$suffix"
}

/**
 * Derives an iOS framework base name from the Gradle module path.
 *
 *  - `:shared`            -> `Shared`
 *  - `:core:designsystem` -> `CoreDesignsystem`
 */
internal fun pathToFrameworkName(project: Project): String =
    project.path
        .removePrefix(":")
        .split(":", "-", "_")
        .filter { it.isNotBlank() }
        .joinToString("") { part -> part.replaceFirstChar { it.uppercaseChar() } }
