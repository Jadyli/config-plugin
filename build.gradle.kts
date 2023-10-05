@file:Suppress("UnstableApiUsage")

import com.jady.lib.config.ConfigExtension

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(androidCommonLibs.plugins.android.application) apply false
    alias(androidCommonLibs.plugins.android.library) apply false
    alias(sharedCommonLibs.plugins.kotlin.android) apply false
    alias(sharedCommonLibs.plugins.kotlin.kapt) apply false
    alias(sharedCommonLibs.plugins.compose) apply false
    alias(androidCommonLibs.plugins.config.plugin) apply false
}

val libs = androidCommonLibs
val bizLibs = androidBizLibs
val sharedLibs = sharedCommonLibs
subprojects {
    apply(plugin = libs.plugins.config.plugin.get().pluginId)
    extensions.configure<ConfigExtension> {
        version {
            minSdk = bizLibs.versions.minSdk.get().toInt()
            targetSdk = bizLibs.versions.targetSdk.get().toInt()
            compileSdk = libs.versions.compileSdk.get().toInt()
            java = libs.versions.java.asProvider().get().toInt()
            kotlin = sharedLibs.versions.kotlin.asProvider().get()
            composeCompiler = sharedLibs.versions.compose.compiler.get()
        }
        vectorDrawableSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
