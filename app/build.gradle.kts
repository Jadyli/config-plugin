@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(commonLibs.plugins.android.application)
    alias(commonLibs.plugins.kotlin.android)
    alias(commonLibs.plugins.hilt.android)
    alias(commonLibs.plugins.config.plugin)
    alias(commonLibs.plugins.kotlin.kapt)
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
    implementation(commonLibs.hilt.runtime)
    implementation(commonLibs.kotlin.stdlib)
    implementation(commonLibs.kotlin.coroutines.core)
    implementation(commonLibs.androidx.lifecycle.runtime.ktx)
    implementation(commonLibs.androidx.appcompat)
    implementation(commonLibs.bundles.compose.core)

    kapt(commonLibs.bundles.hilt.compiler)

    // other module
    implementation(projects.common.common)

    // test
    testImplementation(commonLibs.bundles.test)
}
