plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.graph"


dependencies {
    implementation(libs.kotlinxCoroutines)
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:visualizer:api"))
}
