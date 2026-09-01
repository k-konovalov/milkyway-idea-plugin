plugins {
    alias(libs.plugins.kotlin)
}

group = "com.github.milkyway.algorithm"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
}
