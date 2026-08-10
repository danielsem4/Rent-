package org.example.rent.convention

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Room convention — KMP Room database wiring (Room runtime + bundled SQLite + KSP compiler
 * for every target). Apply on modules that own a Room database.
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("androidx.room")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            add("commonMainImplementation", libs.findLibrary("room-runtime").get())
            add("commonMainImplementation", libs.findLibrary("sqlite-bundled").get())
            add("kspAndroid", libs.findLibrary("room-compiler").get())
            add("kspIosArm64", libs.findLibrary("room-compiler").get())
            add("kspIosSimulatorArm64", libs.findLibrary("room-compiler").get())
        }
    }
}
