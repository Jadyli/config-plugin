package com.jady.lib.config

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.google.devtools.ksp.gradle.KspExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import io.github.skeptick.libres.plugin.LibresResourcesGenerationTask
import io.github.skeptick.libres.plugin.ResourcesPlugin
import io.github.skeptick.libres.plugin.ResourcesPluginExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomScm
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.typeOf
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * @author jady
 * @since 2021-12-15, 周三, 0:5
 * email: 1257984872@qq.com
 */
private const val TARGET_NAME_IOS_X64 = "iosX64"
private const val TARGET_NAME_DESKTOP = "desktop"
private const val TARGET_NAME_ANDROID = "android"
private const val TARGET_NAME_JS = "js"
private const val TARGET_NAME_WASM_JS = "wasmJs"

@Suppress("UnstableApiUsage")
class ConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            val commonConfigExtension = extensions.create("config", CommonConfigExtension::class.java)
            afterEvaluate {
                commonConfigExtension.spotlessAction?.let {
                    pluginManager.apply(SpotlessPlugin::class.java)
                    it.execute(extensions.getByType(SpotlessExtension::class.java))
                }
                plugins.forEach { plugin ->
                    when (plugin) {
                        is LibraryPlugin -> configureLibraryPlugin(commonConfigExtension)
                        is AppPlugin -> configureAppPlugin(commonConfigExtension)
                        is ResourcesPlugin -> {
                            project.tasks.findByName("prepareKotlinIdeaImport")
                                ?.dependsOn(project.tasks.withType(LibresResourcesGenerationTask::class.java))
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun Project.configureAppPlugin(configExtension: CommonConfigExtension) {
        extensions.getByType<BaseAppModuleExtension>().run {
            configCommonExtension(this@configureAppPlugin, configExtension)
            defaultConfig.proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildTypes {
                getByName("release").run {
                    isShrinkResources = true
                    isMinifyEnabled = true
                }
            }
        }
    }

    private fun Project.configureLibraryPlugin(configExtension: CommonConfigExtension) {
        extensions.getByType<LibraryExtension>().run {
            configCommonExtension(this@configureLibraryPlugin, configExtension)
            defaultConfig {
                consumerProguardFiles("proguard-rules.pro")
                proguardFiles("proguard-rules.pro")
            }
        }
    }

    private fun BaseExtension.configCommonExtension(project: Project, commonConfigExtension: CommonConfigExtension) {
        setCompileSdkVersion(commonConfigExtension.version.compileSdk)
        val javaMajor = commonConfigExtension.version.java
        val javaVersion = JavaVersion.toVersion(javaMajor)
        val javaVersionString = javaVersion.toString()
        val hasComposePlugin = project.plugins.hasPlugin(ComposePlugin::class.java)
        defaultConfig.run {
            minSdk = commonConfigExtension.version.minSdk
            targetSdk = commonConfigExtension.version.targetSdk
            testInstrumentationRunner = commonConfigExtension.testInstrumentationRunner
            vectorDrawables.useSupportLibrary = commonConfigExtension.vectorDrawableSupportLibrary
        }

        @Suppress("deprecation")
        lintOptions.run {
            isAbortOnError = false
            isCheckReleaseBuilds = false
            File("lint.xml").takeIf { it.exists() }?.let {
                lintConfig = it
            }
        }

        val extensions = (this as ExtensionAware).extensions
        val kotlinExtension = extensions.findByName("kotlin") as? KotlinTopLevelExtension
        kotlinExtension?.jvmToolchain(javaMajor)
        val kotlinOptions = extensions.findByName("kotlinOptions") as? KotlinJvmOptions
        kotlinOptions?.setKotlinOptions(javaVersionString, hasComposePlugin)

        project.tasks.findByName("kaptGenerateStubs")?.configure<KaptGenerateStubs> {
            compilerOptions.setKotlinCommonCompilerOptions(hasComposePlugin)
        }
        project.tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions?.setKotlinOptions(javaVersionString, hasComposePlugin)
        }

        sourceSets.run {
            getByName("main").run {
                if (kotlinExtension != null) {
                    manifest.srcFile("src/androidMain/AndroidManifest.xml")
                    res.srcDirs("src/androidMain/res")
                    java.srcDirs("src/androidMain/kotlin", "src/commonMain/kotlin")
                    resources.srcDirs("src/commonMain/resources")
                } else {
                    res.srcDirs("src/main/res")
                    java.srcDirs("src/main/kotlin", "src/main/java")
                }
                jniLibs.srcDirs("libs")
            }
        }

        packagingOptions.run {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        testOptions.run {
            unitTests.isIncludeAndroidResources = true
            unitTests.all { test ->
                test.jvmArgs("-noverify")
            }
        }

        compileOptions.run {
            with(javaVersion) {
                sourceCompatibility = this
                targetCompatibility = this
            }
        }
    }

    private fun KotlinJvmOptions.setKotlinOptions(javaVersion: String, useCompose: Boolean) {
        jvmTarget = javaVersion
        options.setKotlinCommonCompilerOptions(useCompose)
    }

    data class PluginsData(
        var hasComposePlugin: Boolean = false,
        var hasAndroidAppPlugin: Boolean = false,
        var hasLibraryPlugin: Boolean = false,
    )
}

fun KotlinCommonCompilerOptions.setKotlinCommonCompilerOptions(useCompose: Boolean) {
    freeCompilerArgs.addAll(
        arrayListOf(
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xcontext-receivers",
            "-Xskip-prerelease-check",
        ).apply {
            if (useCompose) {
                add("-opt-in=androidx.compose.foundation.ExperimentalFoundationApi")
            }
        }
    )
}

fun <T : Any, U : NamedDomainObjectContainer<T>> U.configureSourceSet(
    sourceSetName: String,
    configuration: T.() -> Unit
): T {
    return getOrCreate(sourceSetName).apply(configuration)
}

fun <T : Any, U : NamedDomainObjectContainer<T>> U.getOrCreate(sourceSetName: String) = findByName(sourceSetName) ?: create(sourceSetName)

fun <T : Any, U : NamedDomainObjectContainer<T>> U.getSourceSet(sourceSetName: String, configuration: (T.() -> Unit)? = null) =
    getByName(sourceSetName).apply { configuration?.invoke(this) }

fun <T : Any, U : NamedDomainObjectContainer<T>> U.createSourceSet(sourceSetName: String, configuration: (T.() -> Unit)? = null) =
    create(sourceSetName).apply { configuration?.invoke(this) }

fun Project.applyMavenPlugin(closure: Action<MavenExtension>) {
    pluginManager.apply(MavenPublishPlugin::class.java)
    val maven = MavenExtension().apply { closure.execute(this) }
    extensions.findByType(PublishingExtension::class.java)?.run {
        maven.mavenRepository?.let { repo ->
            repositories(object : Action<RepositoryHandler> {
                override fun execute(handler: RepositoryHandler) {
                    handler.maven(object : Action<MavenArtifactRepository> {
                        override fun execute(mavenArtifactRepository: MavenArtifactRepository) {
                            mavenArtifactRepository.run {
                                name = repo.name
                                url = if (version.toString().endsWith("SNAPSHOT")) {
                                    uri(repo.snapshotUrl)
                                } else {
                                    uri(repo.releaseUrl)
                                }
                            }
                        }
                    })
                }
            })
        }
    }
    extensions.findByType(MavenPublishBaseExtension::class.java)?.run {
        maven.pom?.let { pom ->
            pom(object : Action<MavenPom> {
                override fun execute(mavenPom: MavenPom) {
                    mavenPom.run {
                        name.set(project.name)
                        description.set("Module of $name")
                        url.set(pom.repoUrl)
                        scm(object : Action<MavenPomScm> {
                            override fun execute(mavenPomScm: MavenPomScm) {
                                mavenPomScm.run {
                                    connection.set(pom.httpsConnection)
                                    developerConnection.set(pom.gitConnection)
                                    url.set(pom.repoUrl)
                                }
                            }
                        })
                        developers(object : Action<MavenPomDeveloperSpec> {
                            override fun execute(mavenPomDeveloperSpec: MavenPomDeveloperSpec) {
                                mavenPomDeveloperSpec.run {
                                    developer(object : Action<MavenPomDeveloper> {
                                        override fun execute(mavenPomDeveloper: MavenPomDeveloper) {
                                            mavenPomDeveloper.run {
                                                name.set(pom.developerName)
                                            }
                                        }
                                    })
                                }
                            }
                        })
                    }
                }
            })
        }
    }
}

fun Project.applyLibResPlugin(closure: Action<ResourcesPluginExtension>) {
    pluginManager.apply(ResourcesPlugin::class.java)
    closure.execute(project.extensions.getByType(ResourcesPluginExtension::class.java))
}

fun KotlinMultiplatformExtension.configKMPPlugin(
    project: Project,
    javaVersion: Int,
    composePluginCompiler: String?,
    applyDefaultTargets: Boolean
) {
    jvmToolchain(javaVersion)
    if (applyDefaultTargets) {
        androidTarget()
        jvm("desktop")
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }
    cocoapodsExtensionOrNull?.configCocoapods(project)
    targets.forEach { target ->
        target.compilations.forEach {
            it.kotlinOptions.options.setKotlinCommonCompilerOptions(true)
            it.compilerOptions.configure {
                setKotlinCommonCompilerOptions(true)
                jvmToolchain(javaVersion)
            }
        }
    }
    project.composeExtensionOrNull?.run {
        composePluginCompiler?.let { kotlinCompilerPlugin.set(it) }
    }
    configKMPSourceSets(project)
}

private val Project.composeExtensionOrNull: ComposeExtension?
    get() = extensions.findByType(ComposeExtension::class.java)

private val KotlinMultiplatformExtension.cocoapodsExtensionOrNull: CocoapodsExtension?
    get() = (this as ExtensionAware).extensions.findByType(CocoapodsExtension::class.java)

private fun CocoapodsExtension.configCocoapods(project: Project) {
    name = project.name
    version = "1.0.0"
    ios.deploymentTarget = "14.1"
    val preferDir = File(project.rootDir.path + "/iosApp/Podfile")
    podfile = if (preferDir.exists()) preferDir else File(project.rootDir.parentFile.path + "/iosApp/Podfile")
    framework {
        baseName = project.name
        isStatic = true
    }
}

private fun KotlinMultiplatformExtension.configKMPSourceSets(project: Project) {
    sourceSets.run {
        val compose = project.extensions.findByType(typeOf<ComposeExtension>())?.dependencies
        val commonMain by getting {
            dependencies {
                compose?.run {
                    api(runtime)
                    api(foundation)
                    api(material)
                    api(material3)
                    @OptIn(ExperimentalComposeLibrary::class)
                    api(components.resources)
                }
            }
        }
        val jvmCommonMain = configureSourceSet("jvmCommonMain") {
            dependsOn(commonMain)
            dependencies {
                compose?.run {
                    api(uiTooling)
                }
            }
        }
        val jsWasmMain = configureSourceSet("jsWasmMain") {
            dependsOn(commonMain)
        }
        targets.names.forEach {
            when (it) {
                TARGET_NAME_IOS_X64 -> {
                    val iosX64Main by getting
                    val iosArm64Main by getting
                    val iosSimulatorArm64Main by getting
                    configureSourceSet("iosMain") {
                        dependsOn(commonMain)
                        iosX64Main.dependsOn(this)
                        iosArm64Main.dependsOn(this)
                        iosSimulatorArm64Main.dependsOn(this)
                    }
                }
                TARGET_NAME_DESKTOP -> {
                    val desktopMain by getting {
                        kotlin.srcDir("build/generated/ksp/desktop/desktopMain")
                        dependsOn(jvmCommonMain)
                        dependencies {
                            compose?.run {
                                api(desktop.common)
                            }
                        }
                    }
                }
                TARGET_NAME_ANDROID -> {
                    val androidMain by getting {
                        kotlin.srcDir("build/generated/ksp/android/androidDebug")
                        kotlin.srcDir("build/generated/ksp/android/androidRelease")
                        dependsOn(jvmCommonMain)
                    }
                }
                TARGET_NAME_JS -> {
                    val jsMain by getting {
                        kotlin.srcDir("build/generated/ksp/js/jsMain")
                        dependsOn(jsWasmMain)
                    }
                }
                TARGET_NAME_WASM_JS -> {
                    val wasmJsMain by getting {
                        kotlin.srcDir("build/generated/ksp/wasm/wasmJsMain")
                        dependsOn(jsWasmMain)
                    }
                }
            }
        }
    }
}

fun DependencyHandlerScope.addKspDependencies(kspCompilerList: List<KspCompiler>) {
    println("ksp: $kspCompilerList")
    kspCompilerList.forEach { (isMultiplatform, dependency) ->
        if (!isMultiplatform) {
            add("ksp", dependency)
        } else {
            add("kspDesktop", dependency)
            add("kspAndroid", dependency)
            add("kspIosX64", dependency)
            add("kspIosArm64", dependency)
            add("kspIosSimulatorArm64", dependency)
        }
    }
}

fun KspExtension.configKspExtension() {
    arg("KOIN_CONFIG_CHECK", "true")
}
