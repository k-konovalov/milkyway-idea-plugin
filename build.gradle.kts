plugins {
    kotlin("jvm") version "2.1.21" apply false
    kotlin("plugin.serialization") version "2.1.21" apply false
    id("org.jetbrains.intellij.platform") version "2.16.0" apply false
}

allprojects {
    group = "com.github.milkyway"
    version = "0.1.0"
}