/*
 * Copyright (c) 2015-2024 BiliBili Inc.
 */

package com.jady.lib.config

import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.JavaExtension
import com.diffplug.gradle.spotless.KotlinExtension
import com.diffplug.gradle.spotless.KotlinGradleExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Action
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

/**
 * @author jady
 * @since 2023/09/04 20/26
 * email: 1257984872@qq.com
 */
abstract class CommonConfigExtension {
    var vectorDrawableSupportLibrary = false
    var testInstrumentationRunner: String? = null

    internal var version = VersionExtension()
    internal var spotlessAction: Action<SpotlessExtension>? = null

    fun version(closure: Action<VersionExtension>) {
        closure.execute(version)
    }

    fun spotless(closure: Action<SpotlessExtension>) {
        spotlessAction = closure
    }

    companion object {
        val DEFAULT_SPOTLESS_CONFIG_ACTION = object : Action<SpotlessExtension> {
            override fun execute(extension: SpotlessExtension) {
                extension.run {
                    format("misc", object : Action<FormatExtension> {
                        override fun execute(extension: FormatExtension) {
                            extension.run {
                                target("*.md", ".gitignore")
                                trimTrailingWhitespace()
                                endWithNewline()
                            }
                        }
                    })
                    java(object : Action<JavaExtension> {
                        override fun execute(extension: JavaExtension) {
                            extension.run {
                                removeUnusedImports()
                                indentWithSpaces(4)
                                toggleOffOn()
                            }
                        }
                    })
                    kotlin(object : Action<KotlinExtension> {
                        override fun execute(extension: KotlinExtension) {
                            extension.run {
                                target("**/*.kt")
                                targetExclude(
                                    "**/copyright.kt",
                                )
                                ktlint("1.0.1")
                                    .editorConfigOverride(
                                        mapOf(
                                            "indent_size" to 4,
                                            "max-linelength" to 140,
                                            "ktlint_standard_discouraged-comment-location" to "disabled",
                                            "ktlint_standard_spacing-between-declarations-with-comments" to "disabled",
                                            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                                            "spacing-between-declarations-with-annotations" to "disabled",
                                            "spacing-between-declarations-with-comments" to "disabled",
                                            "multiline-if-else" to "disabled",
                                            "ij_kotlin_allow_trailing_comma_on_call_site" to false,
                                            "ij_kotlin_allow_trailing_comma" to false
                                        )
                                    )
                                trimTrailingWhitespace()
                                endWithNewline()
                                toggleOffOn()
                            }
                        }
                    })
                    kotlinGradle(object : Action<KotlinGradleExtension> {
                        override fun execute(extension: KotlinGradleExtension) {
                            extension.run {
                                target("**/*.kts")
                                ktlint("1.0.1")
                                    .editorConfigOverride(
                                        mapOf(
                                            "indent_size" to 4,
                                            "max-linelength" to 140,
                                            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                                            "spacing-between-declarations-with-annotations" to "disabled",
                                            "spacing-between-declarations-with-comments" to "disabled",
                                            "multiline-if-else" to "disabled",
                                            "ij_kotlin_allow_trailing_comma_on_call_site" to false,
                                            "ij_kotlin_allow_trailing_comma" to false
                                        )
                                    )
                                trimTrailingWhitespace()
                                endWithNewline()
                            }
                        }
                    })
                }
            }
        }
    }
}


data class VersionExtension(
    var minSdk: Int = 21,
    var targetSdk: Int = 30,
    var compileSdk: Int = 33,
    var java: Int = 11,
    var kotlin: String = "2.0.0",
)

data class KspCompiler(
    var isMultiplatform: Boolean,
    var onlyUseInCommon: Boolean,
    var dependency: Provider<MinimalExternalModuleDependency>
)

data class MavenExtension(var mavenRepository: MavenRepository? = null, var pom: PomExtension? = null)

data class MavenRepository(var name: String = "", var releaseUrl: String = "", var snapshotUrl: String = releaseUrl)

data class PomExtension(
    var repoUrl: String = "",
    var httpsConnection: String = "",
    var gitConnection: String = "",
    var developerName: String = ""
)
