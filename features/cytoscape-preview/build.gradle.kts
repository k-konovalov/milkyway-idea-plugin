plugins {
    alias(libs.plugins.kotlinBase)
    application
}

group = "com.github.milkyway.tools"

application {
    mainClass.set("com.github.milkyway.tools.MainKt")
}

dependencies {
    implementation(project(":features:parser:regex"))
    implementation(project(":features:parser:api"))
    implementation(project(":features:graph-processing:impl"))
    implementation(project(":features:visualizer:cytoscape:api"))
}

tasks.register<JavaExec>("generateCytoscapeHtml") {
    group = "milkyway"
    description = "Parse a Gradle project by path and emit cytoscape.html to <projectPath>/build/output/"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.milkyway.tools.MainKt")
    workingDir = rootProject.projectDir
    jvmArgs("-Xmx2g")

    val projectPath = providers.gradleProperty("projectPath")
    args(projectPath.getOrElse(""))
}

tasks.register<Jar>("fatJar") {
    group = "milkyway"
    description = "Builds a standalone fat JAR with all dependencies bundled."

    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets["main"].output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    manifest {
        attributes["Main-Class"] = "com.github.milkyway.tools.MainKt"
    }
}
