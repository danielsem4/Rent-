package org.example.rent.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Classic Android application convention (the `:androidApp` module).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9 ships built-in Kotlin support, so the standalone `org.jetbrains.kotlin.android`
        // plugin must NOT be applied — `com.android.application` registers the `kotlin` extension.
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
            defaultConfig {
                minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
                targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
