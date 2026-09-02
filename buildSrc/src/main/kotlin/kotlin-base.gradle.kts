plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain(17)
}

detekt {
    autoCorrect = false
    basePath = projectDir.absolutePath
    buildUponDefaultConfig = true
    parallel = true
    baseline = rootProject.file("buildSrc/detekt-baseline.xml")
    source.setFrom("src/main/java", "src/main/kotlin")
    config.setFrom(rootProject.files("buildSrc/detekt.yml"))
    reports {
      html.required = false
      md.required = true
      sarif.required = false
      txt.required = false
      xml.required = true
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

// detektBaseline writes per-module so Gradle can track unique outputs (parallel-safe).
// mergeDetektBaselines then folds them into buildSrc/detekt-baseline.xml.
tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    baseline.set(project.layout.projectDirectory.file("detekt-baseline.xml"))
    outputs.upToDateWhen { false }
}

dependencies {
    val versions: Map<String, String> = TomlVersions.parse(rootDir)

    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:${versions["junit"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions["kotlinxCoroutines"]}")
}
