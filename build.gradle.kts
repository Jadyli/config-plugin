@file:Suppress("UnstableApiUsage")

ext {
    set("minSdk", bizLibs.versions.minSdk.get())
    set("targetSdk", bizLibs.versions.targetSdk.get())
    set("compileSdk", libs.versions.compileSdk.get())
    set("javaMajor", libs.versions.java.major.get())
    set("javaVersion", libs.versions.java.asProvider().get())
    set("compose", libs.versions.compose.asProvider().get())
    set("composeCompiler", libs.versions.compose.compiler.get())
}
