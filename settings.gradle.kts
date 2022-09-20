@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")
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
                "dagger.hilt.android.plugin" -> {
                    useModule("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
                }
                "com.jady.lib.config-plugin" -> {
                    useModule("com.github.Jadyli.composing:config-plugin:0.1.8")
                }
                "org.jetbrains.kotlin.android" -> {
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
                }
                "com.android.application" -> {
                    useModule("com.android.tools.build:gradle:7.0.3")
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
        create("libs") {
            from(files("${rootDir.path}/.config/dependencies-common.toml"))
        }
    }
}

rootProject.name = "composing"
include(
    ":app",
    ":common:common",
    ":framework:utils",
)
includeBuild("plugins")
