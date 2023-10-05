/*
 * Copyright (c) 2015-2023 BiliBili Inc.
 */

package com.jady.lib.config

import org.gradle.api.Action

/**
 * @author jady
 * @since 2023/09/04 20/26
 * email: 1257984872@qq.com
 */
abstract class ConfigExtension {
    var version = VersionExtension()
    var vectorDrawableSupportLibrary = false
    var testInstrumentationRunner: String? = null
    fun version(closure: Action<VersionExtension>) {
        closure.execute(version)
    }
}

class VersionExtension {
    var minSdk = 21
    var targetSdk = 30
    var compileSdk = 33
    var java = 11
    var kotlin = "1.9.10"
    var composeCompiler: String? = null
}
