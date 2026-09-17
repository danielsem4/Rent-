plugins {
    alias(libs.plugins.convention.cmp.feature)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.home.domain)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
