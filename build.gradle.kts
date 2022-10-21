@file:Suppress("UnstableApiUsage")

ext {
    set("minSdk", bizLibs.versions.minSdk.get())
    set("targetSdk", bizLibs.versions.targetSdk.get())
    set("compileSdk", commonLibs.versions.compileSdk.get())
    set("javaMajor", commonLibs.versions.java.major.get())
    set("javaVersion", commonLibs.versions.java.asProvider().get())
    set("composeCompiler", commonLibs.versions.compose.compiler.get())
}
