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
    implementation(project(":algorithm:api"))
    implementation(project(":algorithm:critical-path"))
    implementation(project(":algorithm:articulation-points"))
    implementation(project(":algorithm:shape-matching"))
    implementation(project(":visualizer:api"))
    implementation(project(":visualizer:cytoscape:api"))
    implementation(project(":visualizer:cytoscape:impl"))

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
