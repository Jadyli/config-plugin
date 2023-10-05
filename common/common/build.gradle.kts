@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(androidCommonLibs.plugins.android.library)
    alias(sharedCommonLibs.plugins.kotlin.android)
    alias(androidCommonLibs.plugins.hilt.android)
    alias(androidCommonLibs.plugins.config.plugin)
    alias(sharedCommonLibs.plugins.kotlin.kapt)
}

dependencies {
    // official library
    implementation(sharedCommonLibs.bundles.compose.core)
    implementation(androidCommonLibs.hilt.runtime)
    kapt(androidCommonLibs.bundles.hilt.compiler)

    // other module
    api(projects.framework.utils)
}
