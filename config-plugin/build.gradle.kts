plugins {
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

group = "com.jady.lib"
version = "0.1.2"

gradlePlugin {
    plugins.register("config-plugin") {
        id = "com.jady.lib.config-plugin"
        implementationClass = "com.jady.lib.config.ConfigPlugin"
    }
}

dependencies {
    implementation(libs.plugin.source.android)
    implementation(libs.plugin.source.kotlin)
}
