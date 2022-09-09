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

        splits {
            abi {
                isEnable = true
                reset()
                include("x86", "armeabi-v7a", "arm64-v8a")
                isUniversalApk = false
            }
        }
    }
}

dependencies {
    // official library
    implementation(libs.hilt.runtime)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.appcompat)
    implementation(libs.bundles.compose.core)

    kapt(libs.bundles.hilt.compiler)

    // other module
    implementation(projects.common.common)

    // test
    testImplementation(libs.bundles.test)
}
