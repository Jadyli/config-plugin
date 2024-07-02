@file:Suppress("UnstableApiUsage")

import com.jady.lib.config.CommonConfigExtension
import com.jady.lib.config.CommonConfigExtension.Companion.DEFAULT_SPOTLESS_CONFIG_ACTION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(androidCommonLibs.plugins.android.application) apply false
    alias(androidCommonLibs.plugins.android.library) apply false
    alias(sharedCommonLibs.plugins.kotlin.android) apply false
    alias(sharedCommonLibs.plugins.kotlin.kapt) apply false
    alias(sharedCommonLibs.plugins.jetbrains.compose) apply false
    alias(sharedCommonLibs.plugins.compose.compiler) apply false
    alias(sharedCommonLibs.plugins.config.plugin) apply false
}

val androidLibs = androidCommonLibs
val bizLibs = androidBizLibs
val sharedLibs = sharedCommonLibs
subprojects {
    apply(plugin = sharedLibs.plugins.config.plugin.get().pluginId)
    extensions.configure<CommonConfigExtension> {
        version {
            minSdk = bizLibs.versions.minSdk.get().toInt()
            targetSdk = bizLibs.versions.targetSdk.get().toInt()
            compileSdk = androidLibs.versions.compileSdk.get().toInt()
            java = androidLibs.versions.java.asProvider().get().toInt()
            kotlin = sharedLibs.versions.kotlin.asProvider().get()
        }
        spotless(DEFAULT_SPOTLESS_CONFIG_ACTION)
        vectorDrawableSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
