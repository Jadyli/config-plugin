@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.config.plugin)
}

android {
    defaultConfig {
        applicationId = "com.jady.app.composing"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // official library
    implementation(libs.hilt.runtime)
    api(libs.kotlin.stdlib.jdk7)
    api(libs.kotlin.coroutines.core)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.appcompat)
    api(libs.compose.icons.extended)
    api(libs.bundles.compose.core)
    api(libs.bundles.compose.navigation)
    api(libs.bundles.compose.jetpack)

    kapt(libs.bundles.hilt.compiler)

    // other module
    implementation(projects.common.common)

    // test
    testImplementation(libs.bundles.test)
}
