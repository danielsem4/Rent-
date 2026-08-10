import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "org.example.rent.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // build-logic is compiled with Gradle's embedded Kotlin, which may be older than the
        // Kotlin used to build the plugin dependencies (AGP/KGP/Compose 2.4.x). Allow reading
        // their newer metadata.
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.buildkonfig.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "org.example.rent.convention.kmp.library"
            implementationClass = "org.example.rent.convention.KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "org.example.rent.convention.cmp.library"
            implementationClass = "org.example.rent.convention.CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "org.example.rent.convention.cmp.feature"
            implementationClass = "org.example.rent.convention.CmpFeatureConventionPlugin"
        }
        register("cmpApplication") {
            id = "org.example.rent.convention.cmp.application"
            implementationClass = "org.example.rent.convention.CmpApplicationConventionPlugin"
        }
        register("androidApplication") {
            id = "org.example.rent.convention.android.application"
            implementationClass = "org.example.rent.convention.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "org.example.rent.convention.android.application.compose"
            implementationClass = "org.example.rent.convention.AndroidApplicationComposeConventionPlugin"
        }
        register("buildKonfig") {
            id = "org.example.rent.convention.buildkonfig"
            implementationClass = "org.example.rent.convention.BuildKonfigConventionPlugin"
        }
        register("room") {
            id = "org.example.rent.convention.room"
            implementationClass = "org.example.rent.convention.RoomConventionPlugin"
        }
    }
}
