package org.example.rent.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Classic Android application convention with Compose enabled (the `:androidApp` module).
 */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(AndroidApplicationConventionPlugin::class.java)
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            add("implementation", libs.findLibrary("compose-uiToolingPreview").get())
            add("debugImplementation", libs.findLibrary("compose-uiTooling").get())
        }
    }
}
