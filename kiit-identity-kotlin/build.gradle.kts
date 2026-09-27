// Root aggregator, no dependencies of its own.
// The library lives in :kiit-identity, demo apps live under :samples.
plugins {
    idea
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.skie) apply false
}

// Keeps IntelliJ from indexing the TypeScript port and sample (node_modules, dist). Defined here
// rather than through "Mark Directory as Excluded" so it survives Gradle re-syncs. The paths are
// one level up because this Gradle root is kiit-identity-kotlin/, not the repo root.
idea {
    module {
        excludeDirs.addAll(
            listOf(
                file("../ports"),
                file("../samples/sample-ts"),
            ),
        )
    }
}
