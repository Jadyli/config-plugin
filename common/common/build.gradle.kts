@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(commonLibs.plugins.android.library)
    alias(commonLibs.plugins.kotlin.android)
    alias(commonLibs.plugins.hilt.android)
    alias(commonLibs.plugins.config.plugin)
    alias(commonLibs.plugins.kotlin.kapt)
}

dependencies {
    // official library
    implementation(commonLibs.bundles.compose.core)
    implementation(commonLibs.hilt.runtime)
    kapt(commonLibs.bundles.hilt.compiler)

    // other module
    api(projects.framework.utils)
}
