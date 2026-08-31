# План: Архитектурный рефакторинг MilkyWay

## Контекст

Плагин монолитен — вся логика в трёх модулях (`core`, `gradle-plugin`, `idea-plugin`).
Цели рефакторинга:
- **Парсер** — интерфейс в `core`, легко добавить новый формат без трогания архитектуры
- **Алгоритмы** — каждый в отдельном модуле, запускаются параллельно через корутины
- **Визуализатор** — интерфейс с `sealed` выводом; поддержка браузера (JCEF) и статики (изображения)

---

## Итоговая структура модулей

```
core/                       DependencyGraph, Node, DTOs, MilkyWayConstants
                            + DependencyResolver (interface, сюда переезжает из idea-plugin)

algorithm/
  api/                      AnalyzerResult (+ вложенный Empty<T>), GraphAnalyzer<R>
  critical-path/            CriticalPathAnalyzer + TarjanSccFinder (internal)
                            CriticalPathResult : AnalyzerResult
  articulation-points/      ArticulationPointsAnalyzer
                            ArticulationPointsResult : AnalyzerResult
  shape-matching/           GraphShapeMatcher + ShapeManager + ShapeMatcher
                            ShapeMatchResult : AnalyzerResult

visualizer/
  api/                      Visualizer, VisualizationOutput (sealed), GraphAnalysisResult
  cytoscape/                CytoscapeVisualizer → VisualizationOutput.Browser.Html

gradle-plugin/              без изменений

idea-plugin/                RegexDependencyResolver, GradleDependencyResolver (impls)
                            AnalysisPipeline (orchestrator)
                            MilkywayPreviewEditor, HtmlRenderer, ...
```

---

## Ключевые интерфейсы

### algorithm:api

```kotlin
interface AnalyzerResult {
    class Empty<T : AnalyzerResult> : AnalyzerResult
}

interface GraphAnalyzer<out R : AnalyzerResult> {
    suspend fun analyze(graph: DependencyGraph): R
}
```

### visualizer:api

```kotlin
// Агрегированный результат всех алгоритмов
class GraphAnalysisResult internal constructor(
    val graph: DependencyGraph,
    private val results: Map<KClass<out AnalyzerResult>, AnalyzerResult>,
) {
    inline fun <reified R : AnalyzerResult> get(): AnalyzerResult =
        results[R::class] ?: AnalyzerResult.Empty<R>()
}

// Вывод визуализатора
sealed class VisualizationOutput {
    sealed class Browser : VisualizationOutput() {
        data class Html(val content: String) : Browser()
    }
    sealed class Static : VisualizationOutput() {
        data class ImageFile(val path: Path) : Static()
    }
}

interface Visualizer {
    fun render(result: GraphAnalysisResult): VisualizationOutput
}
```

### idea-plugin: AnalysisPipeline

```kotlin
class AnalysisPipeline(private val analyzers: List<GraphAnalyzer<out AnalyzerResult>>) {
    suspend fun run(graph: DependencyGraph): GraphAnalysisResult = coroutineScope {
        val results = analyzers
            .map { async { it.analyze(graph) } }
            .awaitAll()
            .associateBy { it::class }
        GraphAnalysisResult(graph, results)
    }
}
```

Каждый алгоритм-модуль добавляет extension на `GraphAnalysisResult`:
```kotlin
// В algorithm:critical-path:
fun GraphAnalysisResult.criticalPath(): AnalyzerResult = get<CriticalPathResult>()
```

Caller различает случаи через `when`:
```kotlin
when (val cp = result.criticalPath()) {
    is AnalyzerResult.Empty<*> -> skip()
    is CriticalPathResult      -> render(cp)
}
```

---

## Фазы реализации

### Фаза 1 — algorithm:api + три алгоритм-модуля

**Затронутые файлы:**
- `settings.gradle.kts` — добавить includes
- Новые `algorithm/api/build.gradle.kts`, `algorithm/critical-path/build.gradle.kts`, etc.
- **Переезжают из `core/`:**
  - `analyzer/CriticalPathAnalyzer.kt` → `algorithm/critical-path/`
  - `analyzer/TarjanSccFinder.kt` → `algorithm/critical-path/` (internal)
  - `analyzer/ArticulationPointsAnalyzer.kt` → `algorithm/articulation-points/`
  - `shape/GraphShapeMatcher.kt`, `ShapeManager.kt`, `ShapeMatcher.kt` → `algorithm/shape-matching/`
  - `models/Shape.kt` — остаётся в `core` (используется в `gradle-plugin`)
- Новые result-типы: `CriticalPathResult`, `ArticulationPointsResult`, `ShapeMatchResult`

**Зависимости новых модулей:**
```
algorithm:api                 → core
algorithm:critical-path       → algorithm:api
algorithm:articulation-points → algorithm:api
algorithm:shape-matching      → algorithm:api
```

### Фаза 2 — DependencyResolver в core

**Затронутые файлы:**
- Новый `core/src/.../DependencyResolver.kt` — перенос интерфейса
- `idea-plugin/src/.../resolver/DependencyResolver.kt` — удалить
- `idea-plugin/src/.../resolver/RegexDependencyResolver.kt` — обновить import
- `idea-plugin/src/.../resolver/GradleDependencyResolver.kt` — обновить import

Фазы 1 и 2 независимы — можно выполнять параллельно.

### Фаза 3 — visualizer:api + visualizer:cytoscape

**Затронутые файлы:**
- `settings.gradle.kts` — добавить includes
- Новые `visualizer/api/build.gradle.kts`, `visualizer/cytoscape/build.gradle.kts`
- `visualizer/api/` — `Visualizer.kt`, `VisualizationOutput.kt`, `GraphAnalysisResult.kt`
- `visualizer/cytoscape/` — `CytoscapeVisualizer.kt` (логика из `ReportBuilder.kt`)

**Зависимости:**
```
visualizer:api       → algorithm:api, core
visualizer:cytoscape → visualizer:api, core
```

### Фаза 4 — рефакторинг idea-plugin

**Затронутые файлы:**
- `idea-plugin/build.gradle.kts` — добавить зависимости на новые модули
- Новый `idea-plugin/src/.../pipeline/AnalysisPipeline.kt`
- `idea-plugin/src/.../GradleDependencyAnalysisRunner.kt` — использует `AnalysisPipeline` + `Visualizer`
- `idea-plugin/src/.../cytoscape/ReportBuilder.kt` — удалить (логика переехала в `visualizer:cytoscape`)
- `idea-plugin/src/.../editor/MilkywayPreviewEditor.kt` — обновить вызов рендера
- `idea-plugin/src/.../MilkyWayReportService.kt` — обновить тип хранимого результата

---

## Что НЕ меняется

- `gradle-plugin/` — полностью без изменений
- `HtmlRenderer.kt` — остаётся в `idea-plugin`, загружает HTML в JCEF
- `GraphCutter.kt`, `MilkyWaySettings` — без изменений
- Bundled JS/HTML (`cytoscape.html`, `cytoscape-view.js`) — без изменений
- `MilkyWayConstants` — остаётся в `core`

---

## Проверка

1. `./gradlew check` — компиляция всех модулей без ошибок
2. `make dev` — sandbox IDE запускается, плагин загружается
3. Открыть тестовый проект из `test/test-project/`, убедиться что граф строится
4. Проверить оба парсера (regex и gradle) через Settings → переключить `parser`
5. Убедиться, что `MilkyWayReportService` корректно кэширует и восстанавливает граф при reopening проекта
