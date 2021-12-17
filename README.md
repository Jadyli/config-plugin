
现在的项目基本都是多模块结构，每次新增模块都复制一遍 `minSdk`、`targetSdk` 之类的通用扩展属性会比较麻烦，应该用通用的插件来管理。
​

## 1 插件使用


插件使用案例：[plugin/sample](https://github.com/Jadyli/composing/tree/feature/sample)
​

插件源码： [feature/plugin](https://github.com/Jadyli/composing/tree/feature/plugin) 


### 1.1 添加依赖
​

**方式一：**
​

```kotlin
// 项目根目录 build.gradle.kts
buildscript {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }

    dependencies {
        classpath("com.github.Jadyli.composing:config-plugin:0.1.2")
    }
}
```
​

**方式二(推荐)：**
​

```kotlin
// 项目根目录 settings.gradle.kts
pluginManagement {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.jady.lib.config-plugin" -> {
                    useModule("com.github.Jadyli.composing:config-plugin:0.1.2")
                }
            }
        }
    }
}
```


### 1.2 配置 sdk 版本：
​

**方式一：在项目根目录 gradle.properties 文件中配置**


```kotlin
minSdk=21
targetSdk=31
compileSdk=31
javaMajor=11
javaVersion=11
compose=1.1.0-beta04
```


**方式二(推荐)：在项目根目录 build.gradle.kts 中配置**


```kotlin
// 这里是使用 version catalog 的写法，也可以直接替换成对应的版本号，参考方式一
ext {
    set("minSdk", libs.versions.minSdk.get())
    set("targetSdk", libs.versions.targetSdk.get())
    set("compileSdk", libs.versions.compileSdk.get())
    set("javaMajor", libs.versions.java.major.get())
    set("javaVersion", libs.versions.java.asProvider().get())
    set("compose", libs.versions.compose.get())
}
```
​

这些都是必填项（compose 也是的，插件会默认添加对应版本的 compose 依赖和配置，如果实在不需要可以评论留言，后续版本可以加判断去掉）。


### 1.3 模块里应用


```kotlin
// 方式一（推荐），配合 version catalog 可以写成 alias(libs.plugins.config.plugin)
plugins {
    id("com.jady.lib.config-plugin")
}

// 方式二
apply(from = "com.jady.lib.config-plugin")
```


添加完成后，一个 library 的配置文件将会非常简洁，只需要添加一些模块需要的插件和依赖就好了。
​

```kotlin
@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.config.plugin)
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
​

## 2 依赖管理


gradle 共享依赖官方文档：[https://docs.gradle.org/current/userguide/platforms.html](https://docs.gradle.org/current/userguide/platforms.html)
​

toml 官网：[https://toml.io/en/](https://toml.io/en/)
​

### 2.1 version catalog
​

以前，依赖的管理一直没有一种统一的方式，有的是通过设置进 project properties 中（比如建里一个 dependencies.gradle 文件管理），有的是建立一个插件工程，把所有的依赖都放插件里，这样有个好处就是这个插件可以多个项目共用，取的时候也是变量的形式来使用，version catalog 跟这种插件的形式比较像，只是 version catalog 是通用 toml 格式的文件来定义依赖的。
​

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
​


1. gradle 支持四种节点：
   1. `[versions]` 用于定义版本号，版本号必须用字符串。
   1. `[libraries]` 用于定义依赖，支持的格式参考上面的例子
   1. `[plugins]` 用于定义插件
   1. `[bundles]` 用于定义依赖组
2. 版本号支持指定单个版本和版本范围。具体规则可以参考 [VersionConstraint](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/VersionConstraint.html) 类的注释，也可以参考 [Declaring Rich Versions](https://docs.gradle.org/current/userguide/rich_versions.html#rich-version-constraints)，这里简单说明下：
   1. 版本范围用区间表示。`(`、`)` 表示开区间，`[`、`]` 表示闭区间。`[` 放在区间右边相当于 `)`，比如 `[1, 2[` 相当于 `[1, 2)`，`]`放在区间左边相当于 `(`，比如 `]1, 2]` 相当于 `(1, 2]`。
   1. 有四种版本匹配模式可以指定：`strictly`、`require`、`prefer`、`reject`。
   1. `require`（不指定模式时，`require` 是默认模式）的版本或版本范围对应的是 [VersionConstraint](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/VersionConstraint.html) 中的 `getRequiredVersion()`，required version 表示最低支持的版本，可以更高，但不能比它低。
   1. `strictly` 表示严格要求的版本，字面意思，`prefer`、`reject` 也是字面意思。



定义的这些依赖可以本地用，也可以发布后使用。
​

### 2.2 本地使用
​

toml 文件位置没有规则，可自定义放置，一般放根目录 `gradle` 目录下，也可以放自己的配置文件目录，只要脚本文件中能引用到。现在可以定义 version catalog 容器了，参考：
​

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
​

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
    `maven-publish`
    `version-catalog`
}

catalog {
    versionCatalog {
        from(files("${rootDir.path}/.config/dependencies-common.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.jady.lib"
            artifactId = "dependencies"
            version = "1.0.0"
            from(components["versionCatalog"])
        }
    }
    repositories {
        maven {
            setUrl("repo.url")
            credentials {
                username = project.property("repo.username").toString()
                password = project.property("repo.password").toString()
            }
        }
    }
}

```
发布完后，更改上面提到的 dependencyResolutionManagement 即可。其他项目也可以通过这种方式共用依赖。
​

```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from("com.mycompany:catalog:1.0")
        }
    }
}
```
​

## 3 插件工程
​

这里是的扩展属性是指这样的（`BaseAppModuleExtension`）：
​

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
​

我们先来创建一个插件项目。在创建之前，先讲下 [Composing builds](https://docs.gradle.org/current/userguide/composite_builds.html)，虽然几年前就有了，但国内好像用的公司不多。那我这里来简单说下，Composing build 我个人更喜欢叫混合编译，也有翻译成复合构建或者组合构建的，我不管，我这里就叫混合编译🧐。


简单来说，混合编译的主要作用就是既可以让小模块作为独立工程编译运行，又可以作为大工程的一员一起编译运行。目前国内做编译加速的思路基本都是模块 aar 化，一个几十个模块的工程，针对没有改动的模块使用 aar 编译， 改动了的模块使用源码编译。实际上，如果模块比较独立，还可以更快一点，就是混合编译，单个模块做为独立工程开发调试，开发完之后啥也不用改，用大工程出包。
​

可以看我们插件的 [master 分支](https://github.com/Jadyli/composing)。
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9c5808414b05474fbdc63d5b31c046f5~tplv-k3u1fbpfcp-zoom-1.image)


注意插件是一个独立的工程，可以单独编译运行，可以切到插件源码 [feature/plugin](https://github.com/Jadyli/composing/tree/feature/plugin) 分支查看。
​

所以这里的插件工程就是采用混合编译的形式，在主项目的 settings.gradle 文件中 `includeBuild("plugins")`，然后其他跟第一节讲的使用方式一样就可以了，是不是很神奇？即使我们在 `pluginManagement` 里设置了对应插件 id 的依赖地址，工程编译还是会使用我们的插件源码！当我们开发完毕，只需要发布新版，注释掉 `includeBuild("plugins")` 就好了。
​

下面来配置插件的 `build.gradle` 。
​

```kotlin
plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "com.jady.lib"
version = "0.1.2"

gradlePlugin {
    plugins.register("config") {
        id = "com.jady.lib.config-plugin"
        implementationClass = "com.jady.lib.config.ConfigPlugin"
    }
}
```
​

引入插件，设置 group 和 version，定义插件 id 和入口，就完成了。先不管插件怎么实现，看下怎么发布到 jitpack。切到  [feature/plugin](https://github.com/Jadyli/composing/tree/feature/plugin) 分支，跑插件工程，jitpack 默认使用 java 1.8 编译，我们这里用的是 java 11，所以需要配置一下 jitpack，根目录添加 `jitpack.yml`，内容如下：
​

```kotlin

jdk:
  - openjdk11
```
然后提交，给提交加个 tag，就可以在 jitpack 跑了（[jitpack 地址](https://jitpack.io/#Jadyli/composing)），点击 Get it 会执行编译。
​

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c158df01b2574e68849889bdcbc89069~tplv-k3u1fbpfcp-zoom-1.image)


看日志文件最后的输出：
​

```kotlin
Build artifacts:
com.github.Jadyli.composing:config-plugin:0.1.2
com.github.Jadyli.composing:com.jady.lib.config-plugin.gradle.plugin:0.1.2
```
你会发现有两个，我们直接用第一个就好啦，具体参考第一节。
​

下面来写插件：
​

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
​

插件很简单，可以配置属性、插件、依赖，看代码都懂，没啥好说的，大家有需求可以评论，上面的 split 应该是有的项目用不了的，也可以根据自己的需求 fork 一份出来随意改。
