plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
}

group = "com.github.milkyway"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven { setUrl("https://repo.gradle.org/gradle/libs-releases") }
    maven { setUrl("https://www.jetbrains.com/intellij-repository/releases") }
    maven { setUrl("https://jitpack.io") }
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
    implementation(project(":features:algorithm:critical-path:api"))
    implementation(project(":features:algorithm:critical-path:impl"))
    implementation(project(":features:algorithm:articulation-points:api"))
    implementation(project(":features:algorithm:articulation-points:impl"))
    implementation(project(":features:algorithm:shape-matching:api"))
    implementation(project(":features:algorithm:shape-matching:impl"))
    implementation(project(":features:visualizer:api"))
    implementation(project(":features:visualizer:cytoscape:api"))
    implementation(project(":features:visualizer:cytoscape:impl"))
    implementation(project(":features:parser:api"))
    implementation(project(":features:parser:regex"))

    intellijPlatform {
        intellijIdea("2025.1") {
            useInstaller = false
        }
        bundledPlugin("com.intellij.gradle")
    }

    implementation(libs.gradleToolingApi)
    implementation(libs.kotlinxSerializationJson)
    runtimeOnly(libs.slf4jSimple)
}


val generateMilkywayInitScript by tasks.registering(Copy::class) {
    val pluginVersion = rootProject.version.toString()

    from("src/main/templates/milkyway-init.gradle")
    into(layout.buildDirectory.dir("generated/resources"))

    inputs.property("gradlePluginVersion", pluginVersion)

    filter {
        it.replace("@GRADLE_PLUGIN_VERSION@", pluginVersion)
    }
}

tasks.processResources {
    dependsOn(generateMilkywayInitScript)
    from(layout.buildDirectory.dir("generated/resources"))
}
