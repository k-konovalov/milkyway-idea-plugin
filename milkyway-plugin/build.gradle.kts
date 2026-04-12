plugins {
    kotlin("jvm") version "2.1.21"
    `java-gradle-plugin`
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()


kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("printDependencies") {
            id = "com.github.milkyway"
            implementationClass = "com.github.milkyway.MilkyWayPlugin"
        }
    }
}