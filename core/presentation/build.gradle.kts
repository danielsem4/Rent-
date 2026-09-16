plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.bundles.koin.common)
            implementation(libs.bundles.lifecycle.common)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
