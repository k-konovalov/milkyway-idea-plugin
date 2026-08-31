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

include(":algorithm:api")
include(":algorithm:critical-path")
include(":algorithm:articulation-points")
include(":algorithm:shape-matching")

include(":visualizer:api")
include(":visualizer:cytoscape:api")
include(":visualizer:cytoscape:impl")
