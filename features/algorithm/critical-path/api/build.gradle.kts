plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.algorithm.criticalpath.api"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
}
