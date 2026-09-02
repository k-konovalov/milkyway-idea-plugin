plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain(17)
}

detekt {
    autoCorrect = false
    buildUponDefaultConfig = true
    parallel = true
    source.setFrom("src/main/java", "src/main/kotlin")
    config.setFrom(rootProject.files("buildSrc/detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    reports {
      html.required = false
      md.required = true
      sarif.required = false
      txt.required = false
      xml.required = false
    }
}

dependencies {
    val versions: Map<String, String> = TomlVersions.parse(rootDir)

    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:${versions["junit"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions["kotlinxCoroutines"]}")
}
