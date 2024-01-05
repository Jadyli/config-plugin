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
        maven { setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { setUrl("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental") }
        maven { setUrl("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") }
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
