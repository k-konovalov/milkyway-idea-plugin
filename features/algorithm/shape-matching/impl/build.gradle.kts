plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.algorithm.shapematching"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:algorithm:shape-matching:api"))
}
