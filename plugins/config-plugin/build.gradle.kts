plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

group = "com.jady.lib"
version = "0.1.2"

gradlePlugin {
    plugins.register("config") {
        id = "com.jady.lib.config-plugin"
        implementationClass = "com.jady.lib.config.ConfigPlugin"
    }
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
