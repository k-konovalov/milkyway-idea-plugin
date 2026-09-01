plugins {
    alias(libs.plugins.kotlin)
}

group = "com.github.milkyway.graph"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinxCoroutines)
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:visualizer:api"))
}
