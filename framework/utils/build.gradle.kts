@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.config.plugin)
}

dependencies {
    // test
    testCompileOnly(libs.junit)
    implementation("androidx.compose.compiler:compiler:1.3.1")
}
