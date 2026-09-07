# Milkyway
IDEA plugin for visualise Gradle module dependencies as a graph on demand during build.gradle editing.

## Main idea
Mobile developers, when editing build.gradle.kts, specify the dependencies between modules to link them and use the imports/classes of the corresponding connected module.  
Over time, these dependencies become quite deep — so deep that they start affecting the build time of the entire project.  
An Milkyway plugin parses build.gradle.kts and displays a graph of module dependencies in real time. 


## Features

### Cytoscape Preview

You can generate a standalone `cytoscape.html` visualization from any Gradle project directory
without running the IntelliJ plugin. Use the `generateCytoscapeHtml` task:

```bash
./gradlew :features:cytoscape-preview:generateCytoscapeHtml \
  -PprojectPath="test/test-project/triangle-20-gradle_9_4_1-shape-test-project/project_kts"
```

The output file is written to `<projectPath>/build/output/cytoscape.html`.

#### Standalone fat JAR

You can also build a standalone distributable JAR with all dependencies bundled:

```bash
./gradlew :features:cytoscape-preview:fatJar
```

Then run it on any machine with Java 21+ without Gradle:

```bash
java -Xmx2g -jar cytoscape-preview-0.1.0-all.jar \
  "path/to/your/gradle/project"
```

> **Note:** For large projects, use `-Xmx2g` (or higher) to avoid `OutOfMemoryError`.
> The `generateCytoscapeHtml` Gradle task already sets this automatically.

## Contribution
Feel free for creating Issues and Pull Requests. See [CONTRIBUTE.md](CONTRIBUTE.md) for details

## Thanks for
- All contributors: past and future
- [laniake-gradle-plugin](https://github.com/inDriver/laniakea-gradle-plugin): for gradle ideas
- [module-graph-assert](https://github.com/jraska/modules-graph-assert): for critical paths ideas
- [lobzik](https://github.com/Mishkun/lobzik]): for metricks (decoupling, critical path)
- [ProjectGenerator](https://github.com/cdsap/ProjectGenerator): for test project
