plugins {
    alias(libs.plugins.convention.cmp.application)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)
            implementation(projects.feature.auth.presentation)
            implementation(projects.feature.auth.data)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
