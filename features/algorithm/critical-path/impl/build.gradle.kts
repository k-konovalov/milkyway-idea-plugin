plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.algorithm.criticalpath"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:algorithm:critical-path:api"))
}
