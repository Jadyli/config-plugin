package com.jady.lib.config

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        configCommonPlugin()
        extensions.getByType<LibraryExtension>().configCommonExtension(this@configureLibraryPlugin)
    }

    private fun Project.configureAppPlugin() {
        configCommonPlugin()
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

            buildTypes {
                getByName("debug") {
                    applicationIdSuffix = ".debug"
                    versionNameSuffix = "-debug"
                }
                getByName("release") {
                    isShrinkResources = true
                }
            }
        }
    }

    private fun BaseExtension.configCommonExtension(project: Project) {
        setCompileSdkVersion(project.property("compileSdk").toString().toInt())

        defaultConfig {
            minSdk = project.property("minSdk").toString().toInt()
            targetSdk = project.property("targetSdk").toString().toInt()
            testInstrumentationRunner = project.property("testInstrumentationRunner").toString()
            vectorDrawables.useSupportLibrary = true
        }

        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
            }
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }

        @Suppress("deprecation")
        lintOptions {
            isAbortOnError = false
        }

        buildFeatures.apply {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = project.property("compose").toString()
        }

        packagingOptions {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        testOptions {
            unitTests.isIncludeAndroidResources = true
        }

        compileOptions {
            with(JavaVersion.toVersion(project.property("javaMajor").toString().toInt())) {
                sourceCompatibility = this
                targetCompatibility = this
            }
        }

        project.tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = project.property("java").toString()
                freeCompilerArgs += listOf(
                    "-Xopt-in=kotlin.ExperimentalStdlibApi",
                    "-Xopt-in=kotlin.RequiresOptIn",
                    "-Xopt-in=kotlin.contracts.ExperimentalContracts",
                    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi"
                )
            }
        }
    }

    private fun Project.configCommonPlugin() {
        plugins.apply("org.jetbrains.kotlin.android")
        plugins.apply("org.jetbrains.kotlin.kapt")
    }
}
