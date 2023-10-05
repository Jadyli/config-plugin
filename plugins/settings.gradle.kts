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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://jitpack.io") }
    }
    versionCatalogs {
        create("androidCommonLibs") { from(files("${rootDir.path}/../.config/dependencies-android-common.toml")) }
        create("androidBizLibs") { from(files("${rootDir.path}/../.config/dependencies-android-biz.toml")) }
        create("sharedCommonLibs") { from(files("${rootDir.path}/../.config/dependencies-shared-common.toml")) }
    }
}

rootProject.name = "plugins"

include(":config-plugin")
