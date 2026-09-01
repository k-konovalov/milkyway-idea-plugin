plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.pluginPublish)
}
group = "io.github.milkyway.gradle"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinxSerializationJson)
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