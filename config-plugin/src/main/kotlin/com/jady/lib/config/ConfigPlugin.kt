package com.jady.lib.config

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import java.io.File
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.COMPILE_ONLY

/**
 * @author jady
 * @since 2021-12-15, 周三, 0:5
 * email: 1257984872@qq.com
 */
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
        extensions.getByType<LibraryExtension>().run {
            configCommonExtension(this@configureLibraryPlugin)
            defaultConfig {
                consumerProguardFiles("proguard-rules.pro")
                proguardFiles("proguard-rules.pro")
            }
        }
    }

    private fun Project.configureAppPlugin() {
        extensions.getByType<BaseAppModuleExtension>().run {
            configCommonExtension(this@configureAppPlugin)
            defaultConfig.proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            buildTypes {
                getByName("release") {
                    isShrinkResources = true
                }
            }
        }
    }

    private fun BaseExtension.configCommonExtension(project: Project) {
        val useCompose = null != project.findProperty("composeCompiler")
        setCompileSdkVersion(project.findProperty("compileSdk")?.toString()?.toInt() ?: 33)

        defaultConfig {
            minSdk = project.findProperty("minSdk")?.toString()?.toInt() ?: 21
            targetSdk = project.findProperty("targetSdk")?.toString()?.toInt() ?: 33
            testInstrumentationRunner = project.findProperty("testInstrumentationRunner")?.toString()
            vectorDrawables.useSupportLibrary =
                project.findProperty("vectorDrawableSupportLibrary") as? Boolean ?: false
        }

        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
            }
            getByName("release") {
                isMinifyEnabled = true
            }
        }

        @Suppress("deprecation")
        lintOptions {
            isAbortOnError = false
            isCheckReleaseBuilds = false
            File("lint.xml").takeIf { it.exists() }?.let {
                lintConfig = it
            }
        }

        buildFeatures.apply {
            if (useCompose) {
                compose = true
            }
        }

        sourceSets {
            getByName("main") {
                jniLibs.srcDirs("libs")
                res.srcDirs("src/main/res")
                java.srcDirs("src/main/kotlin", "src/main/java")
            }
        }

        composeOptions {
            kotlinCompilerExtensionVersion = project.findProperty("composeCompiler")?.toString()
        }

        packagingOptions {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.all {
                it.jvmArgs("-noverify")
            }
        }

        compileOptions {
            with(JavaVersion.toVersion(project.property("javaMajor").toString().toInt())) {
                sourceCompatibility = this
                targetCompatibility = this
            }
        }

        project.tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = project.property("javaVersion").toString()
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xopt-in=kotlin.ExperimentalStdlibApi",
                    "-Xopt-in=kotlin.RequiresOptIn",
                    "-Xopt-in=kotlin.contracts.ExperimentalContracts",
                    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
                )
                if (useCompose) {
                    freeCompilerArgs =
                        freeCompilerArgs + "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi"
                }
            }
        }
    }
}