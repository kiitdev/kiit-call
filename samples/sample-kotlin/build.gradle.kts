plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "sample.SampleKt"
}

dependencies {
    // kiit-context has no serialization surface and no suspend functions, so nothing beyond
    // the module itself is needed.
    implementation(project(":kiit-context"))
}
