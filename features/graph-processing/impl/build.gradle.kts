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
    implementation(project(":features:algorithm:critical-path:api"))
    implementation(project(":features:algorithm:critical-path:impl"))
    implementation(project(":features:algorithm:articulation-points:api"))
    implementation(project(":features:algorithm:articulation-points:impl"))
    implementation(project(":features:algorithm:shape-matching:api"))
    implementation(project(":features:algorithm:shape-matching:impl"))
    implementation(project(":features:graph-processing:api"))
    implementation(project(":features:visualizer:api"))
    implementation(project(":features:visualizer:cytoscape:api"))
    implementation(project(":features:visualizer:cytoscape:impl"))
    implementation(project(":features:parser:api"))
}
