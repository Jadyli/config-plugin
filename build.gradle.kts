@file:Suppress("UnstableApiUsage")

ext {
    set("minSdk", bizLibs.versions.minSdk.get())
    set("targetSdk", bizLibs.versions.targetSdk.get())
    set("compileSdk", libs.versions.compileSdk.get())
    set("javaMajor", libs.versions.java.major.get())
    set("javaVersion", libs.versions.java.asProvider().get())
    set("composeCompiler", libs.versions.compose.compiler.get())
}

// plugins {
//     alias(libs.plugins.spotless) apply false
//     alias(libs.plugins.android.application) apply false
//     alias(libs.plugins.android.library) apply false
//     alias(libs.plugins.kotlin.jvm) apply false
//     alias(libs.plugins.kotlin.android) apply false
//     alias(libs.plugins.kotlin.kapt) apply false
//     alias(libs.plugins.kotlin.serialization) apply false
//     alias(libs.plugins.kotlin.parcelize) apply false
//     alias(libs.plugins.config.plugin) apply false
//     alias(libs.plugins.tinker) apply false
// }
