@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(androidCommonLibs.plugins.android.application)
    alias(sharedCommonLibs.plugins.kotlin.android)
    alias(androidCommonLibs.plugins.hilt.android)
    alias(sharedCommonLibs.plugins.kotlin.kapt)
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
    implementation(androidCommonLibs.hilt.runtime)
    implementation(sharedCommonLibs.kotlin.stdlib)
    implementation(sharedCommonLibs.kotlin.coroutines.core)
    implementation(androidCommonLibs.androidx.lifecycle.runtime.ktx)
    implementation(androidCommonLibs.androidx.appcompat)
    implementation(sharedCommonLibs.bundles.compose.core)

    kapt(androidCommonLibs.bundles.hilt.compiler)

    // other module
    implementation(projects.common.common)

    // test
    testImplementation(androidCommonLibs.junit)
}
