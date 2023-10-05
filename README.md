本文主要内容：

1. 依赖管理：gradle 插件 7.0 以上版本使用 version catalog 进行依赖管理
2. 通用编译扩展属性管理：在插件工程配置各子工程通用的扩展属性、插件、依赖
3. 插件工程：插件工程和主工程混合编译，以及 gradle 插件 7.0 以上版本如何发布到 jitpack。

现在的项目基本都是多模块结构，每次新增模块都复制一遍 `minSdk`、`targetSdk` 之类的通用扩展属性会比较麻烦，应该用通用的插件来管理。

## 1 插件使用

插件使用参考本仓库的编译配置文件。

### 1.1 添加依赖

目前已发布到 gradle 插件仓库：[config-plugin](https://plugins.gradle.org/plugin/io.github.jadyli.config-plugin)。

**方式一：**

```kotlin
// 项目根目录 build.gradle.kts
buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
       classpath("io.github.jadyli:config-plugin:0.1.16")
    }
}
```

**方式二(推荐)：**

```kotlin
// 项目根目录 settings.gradle.kts
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

### 1.2 应用插件，配置参数

参考本仓库 build.gradle.kts, 在根目录 build 文件或者模块 build 文件应用插件。

```kotlin
// 方式一（推荐），配合 version catalog 可以写成 alias(libs.plugins.config.plugin)
plugins {
   id("io.github.jadyli.config-plugin")
}

// 方式二
apply(plugin = "io.github.jadyli.config-plugin")
```

然后配置参数：

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

使用插件后，一个 library 的配置文件将会非常简洁，只需要添加一些模块需要的插件和依赖就好了（下面的例子用了 toml）。

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

## 2 依赖管理

gradle 共享依赖官方文档：[https://docs.gradle.org/current/userguide/platforms.html](https://docs.gradle.org/current/userguide/platforms.html)

toml 官网：[https://toml.io/en/](https://toml.io/en/)

### 2.1 version catalog

以前，依赖的管理一直没有一种统一的方式，有的是通过设置进 project properties 中（比如建里一个 dependencies.gradle 文件管理），有的是建立一个插件工程，把所有的依赖都放插件里，这样有个好处就是这个插件可以多个项目共用，取的时候也是变量的形式来使用，version catalog 跟这种插件的形式比较像，只是 version catalog 是通用 toml 格式的文件来定义依赖的。

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
总结一下: 

1. gradle 支持四种节点：
   1. `[versions]` 用于定义版本号，版本号必须用字符串。
   2. `[libraries]` 用于定义依赖，支持的格式参考上面的例子
   3. `[plugins]` 用于定义插件
   4. `[bundles]` 用于定义依赖组
2. 版本号支持指定单个版本和版本范围。具体规则可以参考 [VersionConstraint](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/VersionConstraint.html) 类的注释，也可以参考 [Declaring Rich Versions](https://docs.gradle.org/current/userguide/rich_versions.html#rich-version-constraints)，这里简单说明下：
   1. 版本范围用区间表示。`(`、`)` 表示开区间，`[`、`]` 表示闭区间。`[` 放在区间右边相当于 `)`，比如 `[1, 2[` 相当于 `[1, 2)`，`]`放在区间左边相当于 `(`，比如 `]1, 2]` 相当于 `(1, 2]`。
   2. 有四种版本匹配模式可以指定：`strictly`、`require`、`prefer`、`reject`。
   3. `require`（不指定模式时，`require` 是默认模式）的版本或版本范围对应的是 [VersionConstraint](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/VersionConstraint.html) 中的 `getRequiredVersion()`，required version 表示最低支持的版本，可以更高，但不能比它低。
   4. `strictly` 表示严格要求的版本，字面意思，`prefer`、`reject` 也是字面意思。

定义的这些依赖可以本地用，也可以发布后使用。

### 2.2 本地使用

toml 文件位置没有规则，可自定义放置，一般放根目录 `gradle` 目录下，也可以放自己的配置文件目录，只要脚本文件中能引用到。现在可以定义 version catalog 容器了，参考：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("${rootDir.path}/.config/dependencies-common.toml"))
        }
    }
}
```

sync 完之后会根据 toml 文件生成对应的 java 代码，你在 `build.gradle` 中已经能使用 `libs` 变量了。举个例子：

```kotlin
[versions]
kotlin = "1.6.0"
kotlin-coroutines = "1.6.0-RC2"
[libraries]
kotlin-stdlib-jdk7 = { module = "org.jetbrains.kotlin:kotlin-stdlib-jdk7", version.ref = "kotlin" }
[bundles]
compose-core = ["androidx-activity-compose", "compose-uiTooling", "compose-material"]
[plugins]
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

对应的代码调用为：
val kotlinVersion = libs.versions.kotlin.asProvider().get()
val kotlinCoroutinesVersion = libs.versions.kotlin.coroutines.get()
implementation(libs.kotlin.stdlib.jdk7)
implementation(libs.bundles.compose.core)

// 当前版本的 as 对 version catalog 支持不好，需要加下 @Suppress。
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.android)
}
```

### 2.3 发布到仓库

发布代码：
```kotlin
@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
   alias(androidCommonLibs.plugins.kotlin.dsl)
   alias(androidCommonLibs.plugins.gradle.publish)
}

group = "io.github.jadyli"
version = "0.1.14"

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

dependencies {
   implementation(androidCommonLibs.plugin.source.android)
   implementation(androidCommonLibs.plugin.source.kotlin)
}

```
发布完后，更改上面提到的 dependencyResolutionManagement 即可。其他项目也可以通过这种方式共用依赖。

```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from("com.mycompany:catalog:1.0")
        }
    }
}
```

## 3 插件工程

这里是的扩展属性是指这样的（`BaseAppModuleExtension`）：

```kotlin
android {
    compileSdk 30

    defaultConfig {
        applicationId "com.jady.sample"
        minSdk 21
        targetSdk 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
}
```

多模块的时候，`buildTypes`、`compileOptions` 这样的属性，每个模块都配置一遍倒是不算太麻烦的，麻烦的是每次修改的时候，每个模块都要改，这对于维护来说是非常不友好的。在插件里处理掉这些属性是比较好的一种做法。

我们先来创建一个插件项目。在创建之前，先讲下 [Composing builds](https://docs.gradle.org/current/userguide/composite_builds.html)，虽然几年前就有了，但国内好像用的公司不多。那我这里来简单说下，Composing build 我个人更喜欢叫混合编译，也有翻译成复合构建或者组合构建的，我不管，我这里就叫混合编译🧐。

简单来说，混合编译的主要作用就是既可以让小模块作为独立工程编译运行，又可以作为大工程的一员一起编译运行。目前国内做编译加速的思路基本都是模块 aar 化，一个几十个模块的工程，针对没有改动的模块使用 aar 编译， 改动了的模块使用源码编译。实际上，如果模块比较独立，还可以更快一点，就是混合编译，单个模块做为独立工程开发调试，开发完之后啥也不用改，用大工程出包。

可以看我们插件的 [master 分支](https://github.com/Jadyli/composing)。
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/68e49e60e09f47488deb221a5a87d20b~tplv-k3u1fbpfcp-zoom-1.image)

注意插件是一个独立的工程，可以单独编译运行，可以切到插件源码 [feature/plugin](https://github.com/Jadyli/composing/tree/feature/plugin) 分支查看。

所以这里的插件工程就是采用混合编译的形式，在主项目的 settings.gradle 文件中 `includeBuild("plugins")`，然后其他跟第一节讲的使用方式一样就可以了，是不是很神奇？即使我们在 `pluginManagement` 里设置了对应插件 id 的依赖地址，工程编译还是会使用我们的插件源码！当我们开发完毕，只需要发布新版，注释掉 `includeBuild("plugins")` 就好了。

当然 `includeBuild` 也可以用于普通工程，和插件工程稍微有点不同，比如有如下结构的多个工程：

```kotlin
------------------------------------------------------------
Root project 'ProjectA'
------------------------------------------------------------

Root project 'ProjectA'
+--- Project ':app'

Included builds
\--- Included build ':ProjectB'


Project ':ProjectB'
+--- Project ':Android:moduleA'
+--- Project ':ProjectB:moduleB'
```
在 `ProjectA` 里 `includeBuild` 了 `ProjectB`，`:ProjectA:app`想要使用 `:ProjectB:moduleB`，你可能会这样写：
```kotlin
// :ProjectA:app build.gradle
dependencies {
	implementation project(':ProjectB:moduleB')
}
```

你会发现，找不到这个模块，因为这个 `project()` 方法是 `app` 模块的，这个模块以及 `rootProject` （`projectA`）都不持有 `moduleB`，`moduleB` 是属于 `ProjectB` 的，所以这样写不行。

正确的做法是，用依赖库的形式，就是 `moduleB` 发布到 `maven` 时需要填写 `group`、`name`、`version`，在 `app` 模块的 `build.gradle` 文件也是这样写，比如：

```kotlin
implementation 'com.jady.demo:moduleB:1.0'
```

然后在 `ProjectA` 的 `settings.gradle` 中加入混合编译的代码：

```kotlin
includeBuild("../ProjectB"){
    dependencySubstitution {
        substitute(module("com.jady.demo:moduleB:1.0")).using(project(":moduleB"))
    }
}
```

含义很明显，使用源码工程依赖替换这个 maven 的依赖，所以这个 `moduleB` 可以不用实际发布到 `maven`，反正会被替换。

下面来配置插件的 `build.gradle` 。

```kotlin
plugins {
   alias(androidCommonLibs.plugins.kotlin.dsl)
   alias(androidCommonLibs.plugins.gradle.publish)
}

group = "io.github.jadyli"
version = "0.1.14"

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
```

引入插件，设置 group 和 version，定义插件 id 和入口，就完成了。

目前已经不发布到 jitpack 了，直接发布到 gradle 中心了，所以下面的过程可以忽略，实在需要发布到 jitpack 的也可以参考。

~~看下怎么发布到 jitpack。切到  ~~[~~feature/plugin~~](https://github.com/Jadyli/composing/tree/feature/plugin)~~ 分支，跑插件工程，jitpack 默认使用 java 1.8 编译，我们这里用的是 java 11，所以需要配置一下 jitpack，根目录添加 ~~`~~jitpack.yml~~`~~，内容如下：~~

```kotlin

jdk:
  - openjdk11
```
~~然后提交，给提交加个 tag，就可以在 jitpack 跑了（~~[~~jitpack 地址~~](https://jitpack.io/#Jadyli/composing)~~），点击 Get it 会执行编译。~~

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6aec01b8cb284319b04b54190ad5da08~tplv-k3u1fbpfcp-zoom-1.image)

看日志文件最后的输出：

```kotlin
Build artifacts:
com.github.Jadyli.composing:config-plugin:0.1.2
com.github.Jadyli.composing:com.jady.lib.config-plugin.gradle.plugin:0.1.2
```
~~你会发现有两个，我们直接用第一个就好啦，具体参考第一节。~~

下面来写插件：

```kotlin
@Suppress("UnstableApiUsage")
class ConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            plugins.all {
                when (this) {
                    is LibraryPlugin -> configureLibraryPlugin()
                    is AppPlugin -> configureAppPlugin()
                }
            }
        }
    }

    private fun Project.configureLibraryPlugin() {
        configCommonPlugin()
        configCommonDependencies()
        extensions.getByType<LibraryExtension>().configCommonExtension(this@configureLibraryPlugin)
    }

    private fun Project.configureAppPlugin() {
        configCommonPlugin()
        configCommonDependencies()
        extensions.getByType<BaseAppModuleExtension>().run {
            configCommonExtension(this@configureAppPlugin)

            defaultConfig {
                splits {
                    abi {
                        isEnable = true
                        reset()
                        include("x86", "armeabi-v7a", "arm64-v8a")
                        isUniversalApk = false
                    }
                }
            }

            ...
        }
    }

    private fun BaseExtension.configCommonExtension(project: Project) {
        setCompileSdkVersion(project.property("compileSdk").toString().toInt())
        
       	...
    }

    private fun Project.configCommonPlugin() {
        plugins.apply("org.jetbrains.kotlin.android")
        ...
    }

    private fun Project.configCommonDependencies() {
        ...
    }
}
```

插件很简单，可以配置属性、插件、依赖，看代码都懂，没啥好说的，大家有需求可以评论，上面的 split 应该是有的项目用不了的，也可以根据自己的需求 fork 一份出来随意改。
