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
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:algorithm:critical-path:api"))
    implementation(project(":features:algorithm:articulation-points:api"))
    implementation(project(":features:algorithm:shape-matching:api"))
    implementation(project(":features:visualizer:api"))
    implementation(project(":features:visualizer:cytoscape:api"))
    implementation(project(":features:algorithm:critical-path:impl"))
    implementation(project(":features:algorithm:articulation-points:impl"))
    implementation(project(":features:algorithm:shape-matching:impl"))
    implementation(libs.kotlinxSerializationJson)
}
