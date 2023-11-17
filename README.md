[中文版](https://juejin.cn/post/7041958178682044447)

Most projects today are structured with multiple modules. Every time a new module is added, it's quite bothersome to copy and paste common extension attributes such as `minSdk`, `targetSdk` etc. A better approach would be to manage them through a common plugin.

## 1 Plugin Usage

Refer to the build configuration files in this repository for plugin usage.

### 1.1 Adding dependencies

The plugin has already been published to the Gradle Plugin Repository: [config-plugin](https://plugins.gradle.org/plugin/io.github.jadyli.config-plugin).

**Method One:**

```kotlin
// Root directory build.gradle.kts file
buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
       classpath("io.github.jadyli:config-plugin:0.1.16")
    }
}
```

**Method Two (Recommended):**

```kotlin
// Root directory settings.gradle.kts file
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "io.github.jadyli.config-plugin" -> {
                   useModule("io.github.jadyli:config-plugin:0.1.16")
                }
            }
        }
    }
}
```

### 1.2 Applying the plugin and configuring parameters

Refer to this repository's build.gradle.kts and apply the plugin in either the root directory or the module build file.

```kotlin
// Method One (recommended), can be written as alias(libs.plugins.config.plugin) with version catalog 
plugins {
   id("io.github.jadyli.config-plugin")
}

// Method Two
apply(plugin = "io.github.jadyli.config-plugin")
```

Then configure your parameters:

```kotlin
extensions.configure<ConfigExtension> {
   version {
      minSdk = bizLibs.versions.minSdk.get().toInt()
      targetSdk = bizLibs.versions.targetSdk.get().toInt()
      compileSdk = libs.versions.compileSdk.get().toInt()
      java = libs.versions.java.asProvider().get().toInt()
      kotlin = sharedLibs.versions.kotlin.asProvider().get()
      composeCompiler = sharedLibs.versions.compose.compiler.get()
   }
   vectorDrawableSupportLibrary = true
   testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}
```

After using the plugin, a library configuration file would be much simpler, only needing to add some module-required plugins and dependencies (the following example uses toml).

```kotlin
@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
}

dependencies {
    // official library
    implementation(libs.bundles.compose.core)
    implementation(libs.hilt.runtime)
    kapt(libs.bundles.hilt.compiler)

    // other module
    api(projects.framework.utils)
}
```

## 2 Dependency Management

Official documentation on Gradle Shared Dependencies: [https://docs.gradle.org/current/userguide/platforms.html](https://docs.gradle.org/current/userguide/platforms.html)

Toml Official website: [https://toml.io/en/](https://toml.io/en/)

### 2.1 Version Catalog

Previously, there was no unified way to manage dependencies. Some used a properties file in the project settings (like creating a dependencies.gradle file), others created a plugin project to hold all dependencies. The advantage of this method is that the plugin can be shared across multiple projects and used as variables when fetching, version catalog is similar to this plugin, except the version catalog defines dependencies in a universal toml format file.

```kotlin
[versions]
# build config
compileSdk = "31"
# official library
kotlin = "1.6.0"
compose = "1.1.0-beta04"
androidx-appcompat = "1.4.0"

[libraries]
# official library
appcompat = { module = "androidx.appcompat:appcompat", version.ref = "androidx-appcompat" }
androidx-activity-compose = "androidx.activity:activity-compose:1.4.0"
compose-material = { module = "androidx.compose.material3:material3", version = "1.0.0-alpha02" }
commons-lang3 = { group = "org.apache.commons", name = "commons-lang3", version = { strictly = "[3.8, 4.0[", prefer="3.9" } }

[plugins]
android-application = { id = "com.android.application", version.ref = "android-plugin" }

[bundles]
compose-core = ["androidx-activity-compose", "compose-uiTooling", "compose-material"]
```

In summary:

1. Gradle supports four types of nodes:
   1. `[versions]` is used to define version numbers, which must be strings.
   2. `[libraries]` is used to define dependencies, the supported format can be referred from the example above
   3. `[plugins]` is used to define plugins
   4. `[bundles]` is used to define dependency groups
2. Version numbers support specifying single versions and version ranges. The specific rules can be referred to the comments of [VersionConstraint](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/VersionConstraint.html) class, or refer to [Declaring Rich Versions](https://docs.gradle.org/current/userguide/rich_versions.html#rich-version-constraints). Here's a simple explanation:
   1. Version ranges are represented by intervals. `(`, `)` represents an open interval, `[`, `]` represents a closed interval. `[` on the right side of the interval is equivalent to `)`, for example `[1, 2[` is equivalent to
