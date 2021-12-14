@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("${rootDir.path}/../.config/dependencies-common.toml"))
        }
    }
}

rootProject.name = "plugins"

include(":config-plugin")
