plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.github.milkyway.visualizer.cytoscape"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation(project(":algorithm:api"))
    implementation(project(":visualizer:api"))
    implementation(project(":visualizer:cytoscape:api"))
    implementation(project(":algorithm:critical-path"))
    implementation(project(":algorithm:articulation-points"))
    implementation(project(":algorithm:shape-matching"))
    implementation(libs.kotlinxSerializationJson)
}
