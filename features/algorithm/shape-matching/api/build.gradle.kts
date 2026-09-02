plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.algorithm.shapematching.api"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
}
