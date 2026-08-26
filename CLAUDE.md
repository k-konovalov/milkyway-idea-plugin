# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## CodeGraph (mandatory before edits)

This repo is indexed by [CodeGraph](https://github.com/colbymchenry/codegraph) — a `.codegraph/` SQLite index is committed at the repo root. **Always query it before grep/find/Read when locating or understanding code**, and before touching any Kotlin symbol.

- Preferred: `codegraph_explore` (MCP tool) — one call returns the verbatim line-numbered source of the matching symbols plus the call paths and blast-radius across `core` / `gradle-plugin` / `idea-plugin`, including the cross-JVM hops that plain grep cannot follow (e.g. `GradleDependencyResolver` → `MilkyWayPlugin` → `PrintDependenciesTask`).
- Shell fallback: `codegraph explore "<symbols or question>"` prints the same output.
- Treat source shown by CodeGraph as already Read — do **not** re-open those files with Read afterwards.
- Name concrete symbols or files in the query (e.g. `"GradleDependencyAnalysisRunner ReportBuilder GraphCutter"`) rather than free-form prose; add a question only if you also need the call flow.
- If the daemon is stale after large refactors, the user can re-run `codegraph init` — do **not** run it yourself.

## Build & run

The project is a multi-module Gradle build (Kotlin DSL) with JVM toolchain 17 across all modules. `Makefile` targets are the fast path:

- `make dev` — publish `:core` and `:gradle-plugin` to Maven Local, then launch a sandboxed IDE with the plugin (`:idea-plugin:runIde`). This is the primary dev loop; the sandbox IDE loads the freshly-published gradle-plugin from mavenLocal via the init script (see "Cross-JVM handoff" below).
- `make publish-core` / `make publish-gradle-plugin` — republish either half without a full runIde. Both use `--no-configuration-cache --rerun-tasks` because the init-script templating and IntelliJ instrumentation don't play nice with the configuration cache.
- `make run-ide` — start the sandbox IDE without republishing (only useful when core/gradle-plugin haven't changed since last publish).
- `./gradlew :idea-plugin:buildPlugin --no-configuration-cache` — produce the shippable `.zip` (this is what CI runs; see `.github/workflows/build-milkyway.yml`).
- `./gradlew check` — run all verifications (there is a `Run Tests` IDE config for this). There are no JVM unit tests under `src/test` today; `test/test-project/` holds generated Gradle sample projects used to exercise the plugin manually inside runIde.

CI (`build-milkyway.yml`) uses JetBrains JBR JDK 21 to build the plugin, but source targets are JDK 17.

## Architecture

Three Gradle modules, published in three different ways:

- **`core`** — pure Kotlin, no IntelliJ deps. Data models (`models/*.kt` — `DependencyGraph`, `Cytoscape*Dto`, `Shape`), analyzers (`CriticalPathAnalyzer`, `ArticulationPointsAnalyzer`, `TarjanSccFinder`), shape matching (`shape/*`), and `GraphDependencyMapper` (DTO↔domain). Shared by both other modules — every cross-module data structure lives here. Published as a plain Maven artifact.
- **`gradle-plugin`** (`io.github.milkyway.gradle`) — Gradle plugin registering the `milkywayAnalyzeDependencies` task (`PrintDependenciesTask`). Runs inside the *user's* Gradle daemon, walks the project's module graph, serializes a `DependencyGraphDto` to `build/reports/milkyway/dependency-graph.json`. Published to Maven Local for dev; distributed via Gradle Plugin Portal (`validate-gradle-plugin-globally` / `publish-gradle-plugin-globally` Make targets).
- **`idea-plugin`** — IntelliJ Platform plugin (`platformVersion=2025.2.6.1`, `pluginSinceBuild=252`). Uses the IntelliJ Platform Gradle Plugin 2.16.0 (`org.jetbrains.intellij.platform`). Depends on `com.intellij.gradle`.

### Cross-JVM handoff (idea-plugin ↔ gradle-plugin)

The two plugins run in **different JVMs**. They communicate over disk in JSON:

1. `idea-plugin`'s `GradleDependencyResolver` writes a temp init script from the templated `idea-plugin/src/main/templates/milkyway-init.gradle` (see `generateMilkywayInitScript` task in `idea-plugin/build.gradle.kts` — `@GRADLE_PLUGIN_VERSION@` is substituted at build time with the root project version).
2. The init script pulls `io.github.milkyway.gradle:gradle-plugin` from mavenLocal (which is why `make dev` runs `publish-gradle-plugin` first) and applies it to the root project.
3. `idea-plugin` invokes the Gradle daemon via `ExternalSystemUtil.runTask` with `--init-script`, blocks on a `CountDownLatch`, then reads and deserializes `build/reports/milkyway/dependency-graph.json`.

Shared filenames and task names live in `core/MilkyWayConstants` — change them there, both sides pick it up.

### Two resolvers

`DependencyResolver` has two implementations, chosen at runtime via `MilkyWaySettings.parser`:

- `RegexDependencyResolver` — parses `build.gradle.kts` text with regex. Fast, no Gradle invocation, but sees only direct declarations.
- `GradleDependencyResolver` — the cross-JVM path above. Accurate (uses Gradle's own configuration resolution) but slow.

`GradleDependencyAnalysisRunner` picks a resolver, runs it, feeds the result through `GraphCutter` (drops nodes unrelated to the current file), then `ReportBuilder` (SCC → critical path → articulation points → shape matching → `CytoscapeReportDto`), then serializes to JSON.

### Editor & rendering

The plugin registers a `FileEditorProvider` (`MilkywaySplitEditorProvider`) with `HIDE_DEFAULT_EDITOR` policy that matches only `settings.gradle.kts` and `build.gradle.kts`. It wraps the standard text editor in `MilkywaySplitEditor` (`TextEditorWithPreview`) alongside `MilkywayPreviewEditor`, which owns a `JBCefBrowser`. Every Gradle file gets its own preview + browser instance.

Live-edit flow: `MilkywayPreviewEditor` listens to the document, regex-parses `include(...)` and `project(...)` sets, and if either set changed, debounces via `Alarm(POOLED_THREAD)` for 1s before rerunning `StartupGraphAnalysis`. The debounce is what keeps typing responsive — do not remove the equality check before the alarm request.

Rendering: `HtmlRenderer` embeds the Cytoscape.js report JSON into the static HTML at `idea-plugin/src/main/resources/web/cytoscape.html` (plus `cytoscape-view.js`, klay/expand-collapse/undo-redo plugins) and loads it into JCEF. Do not fetch these from the web — they are bundled and offline-usable.

Report caching: `MilkyWayReportService` (project-level) writes the latest `cytoscape.json` under `PathManager.getSystemDir()/milkyway/<sha256(basePath)[0..15]>/` so reopening the project shows the last graph without a full reanalysis.

Settings: `MilkyWaySettings` is **app-level** (`Service.Level.APP`, `Storage("milkyway-plugin.xml")`) — settings are shared across all projects, not per-project.

## Docs

`docs/architecture/` contains C4-model PlantUML diagrams (c1..c4) that stay in sync with the source anchors listed in its `README.md`. Update them if you change container/component boundaries, resolver dispatch, or the analytics pipeline ordering (the deterministic ordering in `ReportBuilder` / `GraphDependencyMapper` is called out explicitly because it produces clean JSON diffs on disk).

## Manual test projects

`test/test-project/` holds shape-parameterized sample Gradle projects generated by [ProjectGenerator](https://github.com/cdsap/ProjectGenerator) (triangle/rhombus/flat/rectangle/middle_bottleneck/inverse_triangle × varying Gradle versions). Use them inside a runIde sandbox to exercise the plugin against realistic module topologies. `generate-projects.sh --generate-projects` regenerates them; the tool binary is not checked in — see the header comments for the download URL.

## Contribution constraints (from `CONTRIBUTE.md`)

GitHub flow: PRs go from `features/*` → `main`. Every PR must be tied to an Issue. Commit email must be `*@gmail.com`.
