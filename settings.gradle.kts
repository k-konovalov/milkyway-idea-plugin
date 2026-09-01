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
include(":plugins:gradle-plugin")
include(":plugins:idea-plugin")

include(":features:algorithm:api")
include(":features:algorithm:articulation-points:api")
include(":features:algorithm:articulation-points:impl")
include(":features:algorithm:critical-path:api")
include(":features:algorithm:critical-path:impl")
include(":features:algorithm:shape-matching:api")
include(":features:algorithm:shape-matching:impl")

include(":features:visualizer:api")
include(":features:visualizer:cytoscape:api")
include(":features:visualizer:cytoscape:impl")

include(":features:parser:api")
include(":features:parser:regex")

include(":features:graph-processing:api")
include(":features:graph-processing:impl")
