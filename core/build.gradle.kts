plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

// Published to mavenLocal as `com.github.milkyway:core` (also referenced by the
// gradle-plugin POM) — do not change without re-publishing both.
group = "com.github.milkyway"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinxSerializationJson)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}