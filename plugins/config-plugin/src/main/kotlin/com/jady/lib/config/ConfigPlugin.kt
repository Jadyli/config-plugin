package com.jady.lib.config

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * @author jady
 * @since 2021-12-15, 周三, 0:5
 * email: 1257984872@qq.com
 */
@Suppress("UnstableApiUsage")
class ConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            val configExtension = extensions.create("config", ConfigExtension::class.java)
            afterEvaluate {
                config(configExtension)
            }
        }
    }

    private fun Project.config(configExtension: ConfigExtension) {
        val projectName = name
        println("ConfigPlugin, name: $projectName")
        var hasKmpPlugin = false
        var hasComposePlugin = false
        var hasLibraryPlugin = false
        var hasAppPlugin = false
        var basePluginReady = false
        kotlin.runCatching {
            plugins.also {
                println("ConfigPlugin, $projectName, plugins.size: ${plugins.size}")
            }.forEach {
                when (it) {
                    is LibraryPlugin -> {
                        basePluginReady = true
                        hasLibraryPlugin = true
                    }
                    is AppPlugin -> {
                        basePluginReady = true
                        hasAppPlugin = true
                    }
                    is JavaLibraryPlugin, is KotlinPlatformJvmPlugin -> {
                        basePluginReady = true
                    }
                    is KotlinMultiplatformPlugin -> {
                        hasKmpPlugin = true
                    }
                    is ComposePlugin -> {
                        hasComposePlugin = true
                    }
                }
            }
        }.onFailure {
            println("$projectName, plugins traverse error.")
        }
        if (basePluginReady) {
            println("ConfigPlugin, $projectName, basePluginReady: true")
        } else {
            println("ConfigPlugin, $projectName, base plugin not ready!")
        }
        if (hasLibraryPlugin) {
            configureLibraryPlugin(configExtension, hasKmpPlugin, hasComposePlugin)
            return
        }
        if (hasAppPlugin) {
            configureAppPlugin(configExtension, hasKmpPlugin, hasComposePlugin)
        }
    }

    private fun Project.configureLibraryPlugin(
        configExtension: ConfigExtension,
        hasKmpPlugin: Boolean,
        hasComposePlugin: Boolean
    ) {
        println("ConfigPlugin, $name, configureLibraryPlugin")
        extensions.getByType<LibraryExtension>().run {
            configCommonExtension(this@configureLibraryPlugin, configExtension, hasKmpPlugin, hasComposePlugin)
            defaultConfig {
                consumerProguardFiles("proguard-rules.pro")
                proguardFiles("proguard-rules.pro")
            }
        }
    }

    private fun Project.configureAppPlugin(
        configExtension: ConfigExtension,
        hasKmpPlugin: Boolean,
        hasComposePlugin: Boolean
    ) {
        println("ConfigPlugin, $name, configureAppPlugin")
        extensions.getByType<BaseAppModuleExtension>().run {
            configCommonExtension(this@configureAppPlugin, configExtension, hasKmpPlugin, hasComposePlugin)
            defaultConfig.proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildTypes {
                getByName("release").run {
                    isShrinkResources = true
                    isMinifyEnabled = true
                }
            }
        }
    }

    private fun BaseExtension.configCommonExtension(
        project: Project,
        configExtension: ConfigExtension,
        hasKmpPlugin: Boolean,
        hasComposePlugin: Boolean
    ) {
        println("ConfigPlugin, ${project.name}, configCommonExtension")
        setCompileSdkVersion(configExtension.version.compileSdk)
        val javaMajor = configExtension.version.java
        val javaVersion = JavaVersion.toVersion(javaMajor)
        val javaVersionString = javaVersion.toString()
        if (hasKmpPlugin) {
            project.extensions.getByType<KotlinMultiplatformExtension>().run {
                jvmToolchain(javaMajor)
                targets.forEach { target ->
                    target.compilations.forEach {
                        it.kotlinOptions.setKotlinCommonOptions(true)
                        it.compilerOptions.configure {
                            setKotlinCommonCompilerOptions(true)
                            jvmToolchain(configExtension.version.java)
                        }
                    }
                }
            }
        }
        if (hasComposePlugin) {
            project.extensions.getByType<ComposeExtension>().run {
                kotlinCompilerPlugin.set(configExtension.version.composeCompiler)
                kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${configExtension.version.kotlin}")
            }
        }
        defaultConfig.run {
            minSdk = configExtension.version.minSdk
            targetSdk = configExtension.version.targetSdk
            testInstrumentationRunner = configExtension.testInstrumentationRunner
            vectorDrawables.useSupportLibrary = configExtension.vectorDrawableSupportLibrary
        }

        @Suppress("deprecation")
        lintOptions.run {
            isAbortOnError = false
            isCheckReleaseBuilds = false
            File("lint.xml").takeIf { it.exists() }?.let {
                lintConfig = it
            }
        }

        buildFeatures.apply {
            if (hasComposePlugin) {
                compose = true
            }
        }

        val extensions = (this as org.gradle.api.plugins.ExtensionAware).extensions
        val kotlinExtension = extensions.findByName("kotlin") as? KotlinTopLevelExtension
        kotlinExtension?.jvmToolchain(javaMajor)
        val kotlinOptions = extensions.findByName("kotlinOptions") as? KotlinJvmOptions
        kotlinOptions?.setKotlinOptions(javaVersionString, hasComposePlugin)

        project.tasks.withType<KotlinCompile>().forEach { compiler ->
            compiler.kotlinOptions.setKotlinOptions(javaVersionString, hasComposePlugin)
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

        composeOptions.run {
            if (hasComposePlugin) {
                kotlinCompilerExtensionVersion = configExtension.version.composeCompiler
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

    private fun KotlinCommonOptions.setKotlinCommonOptions(useCompose: Boolean) {
        options.setKotlinCommonCompilerOptions(useCompose)
    }

    private fun KotlinCommonCompilerOptions.setKotlinCommonCompilerOptions(useCompose: Boolean) {
        freeCompilerArgs.addAll(
            arrayListOf(
                "-Xopt-in=kotlin.ExperimentalStdlibApi",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.contracts.ExperimentalContracts",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xcontext-receivers",
                "-Xskip-prerelease-check",
            ).apply {
                if (useCompose) {
                    add("-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi")
                }
            }
        )
    }
}
