plugins {
    alias(libs.plugins.convention.cmp.application)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
