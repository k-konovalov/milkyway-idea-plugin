plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-gradle-plugin`
    `maven-publish`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

gradlePlugin {
    plugins {
        create("milkyway") {
            id = "com.github.milkyway"
            implementationClass = "com.github.milkyway.gradle.MilkyWayPlugin"
        }
    }
}