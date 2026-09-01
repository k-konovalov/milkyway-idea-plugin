plugins {
    alias(libs.plugins.kotlin)
}

group = "com.github.milkyway.algorithm.shapematching"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:algorithm:shape-matching:api"))
}
