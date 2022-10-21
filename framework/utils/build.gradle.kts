@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(commonLibs.plugins.android.library)
    alias(commonLibs.plugins.kotlin.android)
    alias(commonLibs.plugins.config.plugin)
}

dependencies {
    implementation(commonLibs.bundles.compose.core)
    // test
    testCompileOnly(commonLibs.junit)
}
