package org.example.rent.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * Shared application convention (the `:shared` module).
 * Compose library + the iOS framework consumed by the Xcode project + app-level DI/navigation.
 * This is the ONLY convention that declares an iOS framework.
 */
class CmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(CmpLibraryConventionPlugin::class.java)

        val frameworkName = pathToFrameworkName(target)
        extensions.configure<KotlinMultiplatformExtension> {
            targets.withType(KotlinNativeTarget::class.java).configureEach {
                binaries.framework {
                    baseName = frameworkName
                    isStatic = true
                }
            }

            sourceSets.getByName("commonMain").dependencies {
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
