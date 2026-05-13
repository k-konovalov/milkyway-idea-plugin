plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}
group = "io.github.milkyway.gradle"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

gradlePlugin {
    website = "https://github.com/shad-2026-platform-projects/milkyway-idea-plugin"
    vcsUrl = "https://github.com/shad-2026-platform-projects/milkyway-idea-plugin.git"
    plugins {
        create("milkyway") {
            id = "io.github.milkyway.gradle"
            displayName = "MilkyWay Gradle Plugin"
            description = "A plugin that helps you to get dependency graph of your gradle project."
            tags = listOf("api", "idea")
            implementationClass = "io.github.milkyway.gradle.MilkyWayPlugin"
        }
    }
}