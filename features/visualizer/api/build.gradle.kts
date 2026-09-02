plugins {
    alias(libs.plugins.kotlinBase)
}

// Feature-scoped group: keeps the module identity (`group:name`) unique.
// :algorithm:api is also named "api" — same-group same-name projects get
// merged by Gradle's conflict resolution in shared dependency graphs.
group = "com.github.milkyway.visualizer"


dependencies {
    implementation(project(":core"))
    implementation(project(":features:algorithm:api"))
}
