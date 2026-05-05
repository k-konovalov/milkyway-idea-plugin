rootProject.name = "MilkyWay"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven {
            url = uri("https://repo.gradle.org/gradle/libs-releases")
        }
    }
}


dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
            url = uri("https://repo.gradle.org/gradle/libs-releases")
        }
    }
}

include(":core")
include(":gradle-plugin")
include(":idea-plugin")
