# Рефакторинг: приведение структуры модулей к module-structure.md

## Финальная структура

```
features/
  algorithm/
    api/                                      :features:algorithm:api
    articulation-points/
      api/                                    :features:algorithm:articulation-points:api
      impl/                                   :features:algorithm:articulation-points:impl
    critical-path/
      api/                                    :features:algorithm:critical-path:api
      impl/                                   :features:algorithm:critical-path:impl
    shape-matching/
      api/                                    :features:algorithm:shape-matching:api
      impl/                                   :features:algorithm:shape-matching:impl
  visualizer/
    api/                                      :features:visualizer:api
    cytoscape/
      api/                                    :features:visualizer:cytoscape:api
      impl/                                   :features:visualizer:cytoscape:impl
  parser/
    api/                                      :features:parser:api
    regex/                                    :features:parser:regex
plugins/
  gradle-plugin/                              :plugins:gradle-plugin
  idea-plugin/                                :plugins:idea-plugin
core/
```

---

## Фаза 1 — Перемещение директорий

```
algorithm/     → features/algorithm/
visualizer/    → features/visualizer/
gradle-plugin/ → plugins/gradle-plugin/
idea-plugin/   → plugins/idea-plugin/
```

---

## Фаза 2 — settings.gradle.kts

```kotlin
include(":core")
include(":plugins:gradle-plugin")
include(":plugins:idea-plugin")

include(":features:algorithm:api")
include(":features:algorithm:articulation-points:api")
include(":features:algorithm:articulation-points:impl")
include(":features:algorithm:critical-path:api")
include(":features:algorithm:critical-path:impl")
include(":features:algorithm:shape-matching:api")
include(":features:algorithm:shape-matching:impl")

include(":features:visualizer:api")
include(":features:visualizer:cytoscape:api")
include(":features:visualizer:cytoscape:impl")

include(":features:parser:api")
include(":features:parser:regex")
```

---

## Фаза 3 — Разбиение algorithm-модулей на api/impl

### Правило пакетов
- api-модуль: суффикс `.api` → `com.github.milkyway.algorithm.{name}.api`
- impl-модуль: пакет без изменений → `com.github.milkyway.algorithm.{name}`

### articulation-points/api (новый)
- `ArticulationPointsResult.kt` → пакет `com.github.milkyway.algorithm.articulationpoints.api`
- depends on `:core`, `:features:algorithm:api`

### articulation-points/impl (был flat-модуль)
- `ArticulationPointsAnalyzer.kt` — остаётся, пакет без изменений
- depends on `:core`, `:features:algorithm:api`, `:features:algorithm:articulation-points:api`

### critical-path/api (новый)
- `CriticalPathResult.kt` → пакет `com.github.milkyway.algorithm.criticalpath.api`
- depends on `:core`, `:features:algorithm:api`

### critical-path/impl (был flat-модуль)
- `CriticalPathAnalyzer.kt`, `CondensedGraph.kt`, `TarjanSccFinder.kt` — остаются
- depends on `:core`, `:features:algorithm:api`, `:features:algorithm:critical-path:api`

### shape-matching/api (новый)
- `Shape.kt`, `ShapeMatchResult.kt` → пакет `com.github.milkyway.algorithm.shapematching.api`
- depends on `:core`, `:features:algorithm:api`

### shape-matching/impl (был flat-модуль)
- `GraphShapeMatcher.kt`, `ShapeMatcher.kt`, `ShapeManager.kt` — остаются
- depends on `:core`, `:features:algorithm:api`, `:features:algorithm:shape-matching:api`

---

## Фаза 4 — features/parser (Clean Architecture)

### parser/api
- `DependencyResolver.kt` — перенесён из `core/resolver/`
- Пакет: `com.github.milkyway.parser.api`
- depends on `:core`

### parser/regex (чистый Kotlin, без IntelliJ Platform)

```kotlin
package com.github.milkyway.parser.regex

interface GradleFilesProvider {
    fun settingsContent(): String?
    fun buildFiles(): List<Pair<String, String>>  // (relativePath, content)
}

class RegexGradleParser(
    private val filesProvider: GradleFilesProvider
) : DependencyResolver {
    override fun resolve(): DependencyGraph { ... }
    // parseSettingsDeps, parseModuleDeps — private методы, перенесены из RegexDependencyResolver
}
```

- depends on `:core`, `:features:parser:api`

### plugins/idea-plugin — адаптер

```kotlin
// НОВЫЙ — реализует порт через IJ VFS
class IjGradleFilesProvider(private val project: Project) : GradleFilesProvider {
    override fun settingsContent(): String? { /* VirtualFile */ }
    override fun buildFiles(): List<Pair<String, String>> { /* VfsUtilCore traversal */ }
}

// РЕФАКТОРИНГ — тонкий адаптер
class RegexDependencyResolver(private val project: Project) : DependencyResolver {
    override fun resolve() = RegexGradleParser(IjGradleFilesProvider(project)).resolve()
}
```

---

## Фаза 5 — Замена project(":...") во всех build.gradle.kts

| Старый путь | Новый путь |
|---|---|
| `:algorithm:api` | `:features:algorithm:api` |
| `:algorithm:articulation-points` | `:features:algorithm:articulation-points:impl` |
| `:algorithm:critical-path` | `:features:algorithm:critical-path:impl` |
| `:algorithm:shape-matching` | `:features:algorithm:shape-matching:impl` |
| `:visualizer:api` | `:features:visualizer:api` |
| `:visualizer:cytoscape:api` | `:features:visualizer:cytoscape:api` |
| `:visualizer:cytoscape:impl` | `:features:visualizer:cytoscape:impl` |

---

## Фаза 6 — Пакеты и импорты

| Файл | Старый пакет | Новый пакет |
|---|---|---|
| `ArticulationPointsResult.kt` | `...articulationpoints` | `...articulationpoints.api` |
| `CriticalPathResult.kt` | `...criticalpath` | `...criticalpath.api` |
| `Shape.kt` | `...shapematching` | `...shapematching.api` |
| `ShapeMatchResult.kt` | `...shapematching` | `...shapematching.api` |
| `DependencyResolver.kt` | `com.github.milkyway.core.resolver` | `com.github.milkyway.parser.api` |

Файлы с обновлением импортов: `CriticalPathAnalyzer`, `ArticulationPointsAnalyzer`, `GraphShapeMatcher`, `ShapeMatcher`, `CytoscapeVisualizer`, `GradleDependencyAnalysisRunner`, `GradleDependencyResolver`, `RegexDependencyResolver`.

---

## Проверка

```bash
./gradlew check --no-configuration-cache
make dev
```
