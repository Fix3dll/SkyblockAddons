pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}
rootProject.name = "SkyblockAddons"