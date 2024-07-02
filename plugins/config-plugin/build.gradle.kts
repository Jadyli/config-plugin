@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(sharedCommonLibs.plugins.kotlin.dsl)
    alias(sharedCommonLibs.plugins.gradle.publish)
}

group = "io.github.jadyli"
version = "0.1.24"

gradlePlugin {
    website.set("https://github.com/Jadyli/config-plugin")
    vcsUrl.set("https://github.com/Jadyli/config-plugin.git")
    plugins.register("config") {
        id = "io.github.jadyli.config-plugin"
        implementationClass = "com.jady.lib.config.ConfigPlugin"
        displayName = "Common config plugin for Android"
        description = "A plugin help you to config android extension"
        tags.addAll("android", "config")
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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(androidCommonLibs.plugin.source.android)
    implementation(sharedCommonLibs.plugin.source.kotlin)
    implementation(sharedCommonLibs.plugin.source.compose)
    implementation(sharedCommonLibs.plugin.source.libres)
    implementation(sharedCommonLibs.plugin.source.ksp)
    implementation(sharedCommonLibs.plugin.source.maven.publish)
    implementation(androidCommonLibs.plugin.source.spotless)
    compileOnly(gradleApi())
}
