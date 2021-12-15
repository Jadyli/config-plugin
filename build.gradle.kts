@file:Suppress("UnstableApiUsage")

ext {
    set("minSdk", libs.versions.minSdk.get())
    set("targetSdk", libs.versions.targetSdk.get())
    set("compileSdk", libs.versions.compileSdk.get())
    set("javaMajor", libs.versions.java.major.get())
    set("javaVersion", libs.versions.java.asProvider().get())
    set("compose", libs.versions.compose.get())
}
