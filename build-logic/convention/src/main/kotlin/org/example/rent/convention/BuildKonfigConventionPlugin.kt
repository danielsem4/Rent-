package org.example.rent.convention

import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * BuildKonfig convention — compile-time config (the multiplatform replacement for `BuildConfig`).
 * Apply on modules that need generated build constants; add fields in the module's own
 * `buildkonfig { defaultConfigs { buildConfigField(...) } }` block.
 */
class BuildKonfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.codingfeline.buildkonfig")

        extensions.configure<BuildKonfigExtension> {
            packageName = pathToPackageName(target)
        }
    }
}
