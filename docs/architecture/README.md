# Milkyway — Architecture (C4)

Diagrams describe the Milkyway IDEA plugin at four levels of zoom, following the
[C4 model](https://c4model.com/). All files are PlantUML; C1–C3 use the
[C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML) stdlib.

## Reading order

1. `c1-context.puml` — who the plugin serves and which external systems it talks to.
2. `c2-containers.puml` — the logical containers inside the plugin
   (Parser / Algorithm Pipeline / Feature / Visualizer) plus shared models and the report service.
3. `c3-text-to-graph.puml` — components involved in turning `build.gradle.kts`
   text into a `DependencyGraph` (both Regex and Gradle branches, including the
   cross-JVM hop into `gradle-plugin`).
4. `c3-graph-to-report.puml` — components that turn a `DependencyGraph` into rendered HTML:
   `AnalysisPipeline` (parallel analyzers) → `GraphAnalysisResult` → `CytoscapeVisualizer` → HTML.
5. `c4-sequence-open-file.puml` — first render when opening a Gradle file.
6. `c4-sequence-edit-debounce.puml` — how live edits are debounced.
7. `c4-sequence-regex-parse.puml` — regex resolver walk.
8. `c4-sequence-gradle-parse.puml` — Gradle-daemon resolver via init-script.
9. `c4-sequence-analytics.puml` — `AnalysisPipeline` parallel run
   (SCC → critical path, articulation points, shape matching → `CytoscapeVisualizer`).

## Key source anchors

| Diagram element | Source |
| --- | --- |
| Editor wiring | `idea-plugin/src/main/kotlin/com/github/milkyway/idea/editor/*` |
| Orchestrator | `idea-plugin/.../GradleDependencyAnalysisRunner.kt` |
| Analysis pipeline | `idea-plugin/.../pipeline/AnalysisPipeline.kt` |
| Resolver interface | `core/src/main/kotlin/com/github/milkyway/core/resolver/DependencyResolver.kt` |
| Resolvers (impls) | `idea-plugin/.../resolver/{Regex,Gradle}DependencyResolver.kt` |
| Gradle plugin side | `gradle-plugin/src/main/kotlin/io/github/milkyway/gradle/*` |
| Algorithm API | `algorithm/api/src/main/kotlin/com/github/milkyway/algorithm/api/*` |
| Critical path | `algorithm/critical-path/src/main/kotlin/com/github/milkyway/algorithm/criticalpath/*` |
| Articulation points | `algorithm/articulation-points/src/main/kotlin/com/github/milkyway/algorithm/articulationpoints/*` |
| Shape matching | `algorithm/shape-matching/src/main/kotlin/com/github/milkyway/algorithm/shapematching/*` |
| Visualizer API | `visualizer/api/src/main/kotlin/com/github/milkyway/visualizer/api/*` |
| Cytoscape rendering | `visualizer/cytoscape/src/main/kotlin/com/github/milkyway/visualizer/cytoscape/*` |
| Web bundle | `visualizer/cytoscape/src/main/resources/web/*` |
| DTOs / mapper | `core/.../models/*` + `core/.../mapper/GraphDependencyMapper.kt` |
| Report cache | `idea-plugin/.../MilkyWayReportService.kt` |
| Settings | `idea-plugin/.../settings/MilkyWaySettings.kt` |

## Rendering

Any of the standard PlantUML tools work:

```sh
# Single file
plantuml -tsvg docs/architecture/c1-context.puml

# Whole directory
plantuml -tsvg docs/architecture/*.puml
```

The C1–C3 diagrams reach for the C4-PlantUML stdlib via HTTPS `!include`; the
first render may need network access. If you need offline rendering, download
the stdlib and replace the URL with a local path.

## Conventions

- Identifier prefixes: `sys_*` for external systems, `cont_*` for containers,
  `comp_*` for components, `dev` for the sole persona.
- Sequence diagrams are plain UML (no C4 stdlib) — they focus on threading /
  async: `Task.Backgroundable`, `Alarm`, `CountDownLatch`, JCEF UI thread.
- Ordering in `CytoscapeVisualizer` and `GraphDependencyMapper` is deterministic; the
  analytics sequence diagram calls this out because it directly enables clean
  diffs of the cached HTML on disk.
