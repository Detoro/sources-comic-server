
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlinx.rpc) apply false
    alias(ktorLibs.plugins.ktor) apply false
}

subprojects {
    group = "com.toro"
    version = "1.0.0-SNAPSHOT"
}
