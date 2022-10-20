@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.config.plugin)
}

dependencies {
    implementation(libs.bundles.compose.core)
    // test
    testCompileOnly(libs.junit)
}
