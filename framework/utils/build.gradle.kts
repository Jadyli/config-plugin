@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(androidCommonLibs.plugins.android.library)
    alias(sharedCommonLibs.plugins.kotlin.android)
    alias(androidCommonLibs.plugins.config.plugin)
}

android {
    namespace = "com.jady.utils"
}

dependencies {
    implementation(sharedCommonLibs.bundles.compose.core)
    // test
    testCompileOnly(androidCommonLibs.junit)
}
