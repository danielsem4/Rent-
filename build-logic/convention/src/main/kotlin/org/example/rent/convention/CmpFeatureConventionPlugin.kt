package org.example.rent.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Feature (presentation) convention: Compose library + the shared core presentation/design-system
 * modules, Koin, Navigation and lifecycle wired in by default.
 */
class CmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(CmpLibraryConventionPlugin::class.java)

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(project(":core:presentation"))
                implementation(project(":core:designsystem"))
                implementation(libs.findBundle("koin-common").get())
                implementation(libs.findBundle("lifecycle-common").get())
                implementation(libs.findLibrary("navigation-compose").get())
            }
            sourceSets.getByName("androidMain").dependencies {
                implementation(libs.findLibrary("koin-android").get())
            }
        }
    }
}
