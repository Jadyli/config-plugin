@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.config.plugin)
    alias(libs.plugins.kotlin.kapt)
}

dependencies {
    // official library
    implementation(libs.bundles.compose.core)
    implementation(libs.hilt.runtime)
    kapt(libs.bundles.hilt.compiler)

    // other module
    api(projects.framework.utils)
}
