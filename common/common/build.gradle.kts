@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.config.plugin)
}

dependencies {
    // official library
    implementation(libs.bundles.compose.core)
    implementation(libs.hilt.runtime)
    kapt(libs.bundles.hilt.compiler)

    // other module
    api(projects.framework.utils)
}
