plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.algorithm.articulationpoints.api"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
}
