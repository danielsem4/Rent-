plugins {
    alias(libs.plugins.convention.cmp.feature)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.auth.domain)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.qrkit)
            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.camera)
        }
    }
}
