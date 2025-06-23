pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}
rootProject.name = "SkyblockAddons"