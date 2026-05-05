plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij.platform")
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven {
        url = uri("https://repo.gradle.org/gradle/libs-releases")
    }

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":core"))

    intellijPlatform {
        intellijIdea("2025.3")
        bundledPlugin("com.intellij.gradle")
    }

    implementation("org.gradle:gradle-tooling-api:9.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
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