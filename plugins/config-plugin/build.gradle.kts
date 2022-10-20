@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.dsl)
    alias(libs.plugins.gradle.publish)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

group = "com.jady.lib"
version = "0.1.14"

gradlePlugin {
    plugins.register("config") {
        id = "com.jady.lib.config-plugin"
        implementationClass = "com.jady.lib.config.ConfigPlugin"
        displayName = "Common config plugin for Android"
        description = "A plugin help you to config android extension"
    }
}

pluginBundle {
    website = "https://github.com/Jadyli/composing"
    vcsUrl = "https://github.com/Jadyli/composing.git"
    tags = arrayListOf("android", "config")
}

publishing {
    repositories {
        maven {
            setUrl("../../local-repo")
            name = "localRepo"
        }
    }
}

dependencies {
    implementation(libs.plugin.source.android)
    implementation(libs.plugin.source.kotlin)
}
