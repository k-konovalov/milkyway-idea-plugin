import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.intelliJPlatform) apply false
}

// Each module declares its `group` explicitly in its own build script, scoped
// per feature (e.g. `com.github.milkyway.algorithm`), so Gradle module
// identities (`group:name`) stay unique: same-group same-name projects are
// merged by conflict resolution in shared dependency graphs.
allprojects {
    version = "0.1.0"
}
