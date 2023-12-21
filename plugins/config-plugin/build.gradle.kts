@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `kotlin-dsl`
    alias(androidCommonLibs.plugins.gradle.publish)
}

group = "io.github.jadyli"
version = "0.1.17"

gradlePlugin {
    plugins.register("config") {
        id = "io.github.jadyli.config-plugin"
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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(androidCommonLibs.plugin.source.android)
    implementation(sharedCommonLibs.plugin.source.kotlin)
    implementation(sharedCommonLibs.plugin.source.compose)
    compileOnly(gradleApi())
}
