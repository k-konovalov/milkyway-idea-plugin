plugins {
    alias(libs.plugins.kotlinBase)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.github.milkyway.visualizer.cytoscape"


dependencies {
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
