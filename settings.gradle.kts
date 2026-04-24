pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()

        // NeuralNexus Mirror
        maven("https://maven.neuralnexus.dev/mirror")

        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.spongepowered.org/maven")

        // Unimined
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "proxy-compatible-forge"

include(":common")
include(":legacy")
val legacyVersions = listOf(
    "7_10",
    "12_2"
).forEach { version ->
    include(":legacy:v$version")
}

include(":modern")
val modernVersions = listOf(
    "13_2",
    "14_4",
    "16_5",
    "17_1",
    "19_2",
    "20_2",
    "20_4"
).forEach { version ->
    include(":modern:v$version")
}

include(":deobsf")
val deobsfVersions = listOf(
    "26_1"
).forEach { version ->
    include(":deobsf:v$version")
}
