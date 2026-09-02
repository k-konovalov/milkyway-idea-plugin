plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.algorithm.articulationpoints"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:algorithm:articulation-points:api"))
}
