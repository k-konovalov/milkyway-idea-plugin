plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val versions: Map<String, String> = TomlVersions.parse(rootDir)

    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:${versions["junit"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions["kotlinxCoroutines"]}")
}
