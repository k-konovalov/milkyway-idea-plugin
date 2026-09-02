plugins {
    alias(libs.plugins.kotlinBase)
}

group = "com.github.milkyway.parser"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:parser:api"))
}
