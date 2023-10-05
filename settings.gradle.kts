@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("./local-repo/") }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> {
                    useModule("com.android.tools.build:gradle:${requested.version}")
                }
                "com.android.library" -> {
                    useModule("com.android.tools.build:gradle:${requested.version}")
                }
                "org.gradle.kotlin.kotlin-dsl" -> {
                    useModule("org.gradle.kotlin:gradle-kotlin-dsl-plugins:${requested.version}")
                }
                "org.jetbrains.kotlin.android" -> {
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                }
                "dagger.hilt.android.plugin" -> {
                    useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
                }
                "io.github.jadyli.config-plugin" -> {
                    useModule("io.github.jadyli:config-plugin:${requested.version}")
                }
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://jitpack.io") }
    }
    versionCatalogs {
        create("androidCommonLibs") { from(files("${rootDir.path}/.config/dependencies-android-common.toml")) }
        create("androidBizLibs") { from(files("${rootDir.path}/.config/dependencies-android-biz.toml")) }
        create("sharedCommonLibs") { from(files("${rootDir.path}/.config/dependencies-shared-common.toml")) }
    }
}

rootProject.name = "composing"
include(
    ":app",
    ":common:common",
    ":framework:utils",
)
includeBuild("plugins")
