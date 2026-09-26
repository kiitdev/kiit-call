pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Lets Gradle auto-provision a JDK toolchain for compiling/testing when only an older JDK is
    // installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kiit-identity-kotlin"

include(":kiit-identity")
include(":sample-kotlin")

// sample-kotlin stays in the shared ./samples/ folder alongside sample-java/sample-swift, one
// level up from this settings file. sample-java/sample-swift start as empty placeholders, not
// included here until they have real content.
project(":sample-kotlin").projectDir = file("../samples/sample-kotlin")
