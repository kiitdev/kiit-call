plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.skie)
    id("signing")
}

// Single source of truth for the published version, mirroring every other kiit module. Left as
// a placeholder: the starting version and first publish target (GitHub Packages pre-release vs.
// Maven Central stable) are the module owner's call, not something to lock in during scaffolding.
val libraryVersion = "0.0.0"

kotlin {
    jvm {
        compilerOptions {
            // JVM 21 so Kotlin emits PermittedSubclasses for any sealed hierarchies, enabling
            // exhaustive Java pattern-matching `switch`, same as every other kiit KMP module.
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64(), iosX64()).forEach {
        it.binaries.framework {
            baseName = "KiitCall"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // No dependencies. Identity/Agent/Source/About have no date fields and nothing else
            // that needs an external library, only kotlin.uuid.Uuid (stdlib) for Identity's
            // instance default. Zero kiit dependencies too, same as kiit-codes.
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Disabled: SKIE's default analytics upload sends git/hardware/project data to Touchlab. Turn
// on only when that's something explicitly wanted, not because it's a default worth keeping.
skie {
    analytics {
        enabled.set(false)
    }
}

android {
    namespace = "kiit.call"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/**
 * Store the following in ~/.gradle/gradle.properties
 *
 * signingInMemoryKeyPassword=
 * signingInMemoryKey=
 * signing.gnupg.keyName=
 * signing.gnupg.passphrase=
 *
 * Maven local: ~/.m2/repository/dev/kiit/kiit-call/
 */
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    coordinates(
        groupId = "dev.kiit",
        artifactId = "kiit-call",
        version = libraryVersion,
    )
    pom {
        name = "kiit-call"
        description = "Identity, source, and app-description types for who/what/how a call or " +
            "running instance is associated with - Kotlin Multiplatform."
        url = "https://kiit.dev"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "codehelix"
                name = "CodeHelix"
                url = "https://kiit.dev"
            }
        }
        scm {
            url = "https://github.com/kiitdev/kiit-call"
            connection = "scm:git:git://github.com/kiitdev/kiit-call.git"
            developerConnection = "scm:git:ssh://git@github.com/kiitdev/kiit-call.git"
        }
    }
}

detekt {
    config.setFrom("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
    source.setFrom(
        "src/commonMain/kotlin",
        "src/iosMain/kotlin",
    )
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

// The jvm() target compiles to JVM 21 bytecode (see the jvm{} block above), so jvmTest needs to
// run on a matching JVM, same as every other kiit KMP module.
tasks.named<Test>("jvmTest") {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

// Read by the release workflow (once one exists) to derive the git tag/GitHub release name from
// the same version published to Maven Central, same convention as every other kiit module.
tasks.register("printVersion") {
    doLast { println(libraryVersion) }
}
