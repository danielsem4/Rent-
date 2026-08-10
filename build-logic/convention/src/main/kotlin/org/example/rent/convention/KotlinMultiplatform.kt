package org.example.rent.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base configuration shared by every KMP library / feature / application module.
 *
 * Declares the iOS targets (no per-module framework — only the app module publishes one),
 * global compiler options, and the Android target for the modern
 * `com.android.kotlin.multiplatform.library` plugin.
 *
 * The Android side is configured via [KotlinMultiplatformAndroidLibraryTarget] (which extends
 * both `KotlinTarget` and `KotlinMultiplatformAndroidLibraryExtension`) using a lazy
 * `configureEach`, which is configuration-cache safe.
 */
internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) {
    val namespaceValue = pathToPackageName(this)
    val compileSdkValue = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
    val minSdkValue = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

    extension.apply {
        // iOS targets — the framework itself is declared only by the application convention.
        // iosX64 (Intel simulator) is omitted: Compose Multiplatform 1.11.x and lifecycle
        // 2.11.x no longer publish iosX64 artifacts.
        iosArm64()
        iosSimulatorArm64()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        // `configureEach` here is the Gradle Kotlin DSL receiver variant, so `this` is the target.
        targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
            namespace = namespaceValue
            compileSdk = compileSdkValue
            minSdk = minSdkValue

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }

            androidResources {
                enable = true
            }

            withHostTestBuilder {}.configure {
                isIncludeAndroidResources = true
            }

            withDeviceTestBuilder {
                sourceSetTreeName = "test"
            }.configure {
                instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
