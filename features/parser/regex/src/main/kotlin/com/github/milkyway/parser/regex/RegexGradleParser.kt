package com.github.milkyway.parser.regex

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.parser.api.DependencyResolver

class RegexGradleParser(
    private val filesProvider: GradleFilesProvider,
) : DependencyResolver {

    override fun resolve(): DependencyGraph {
        val dependencyGraph = DependencyGraph()
        val result = mutableMapOf<String, MutableList<String>>()
        val includeModules = filesProvider.settingsContent()
            ?.let { parseSettingsDeps(it).toSet() }
            ?: emptySet()

        val gradleFiles = filesProvider.buildFiles()
        for ((relativePath, text) in gradleFiles) {
            val moduleName = moduleNameFromPath(relativePath).removePrefix(":")
            val dependencies = parseModuleDeps(text).map { it.second }
            dependencies
                .map { it.removePrefix(":") }
                .forEach { dependencyGraph.addEdge(moduleName, it) }
            result.computeIfAbsent(moduleName) { mutableListOf() }
                .addAll(dependencies)
        }
        if (includeModules.isNotEmpty()) {
            var filtered = result
                .filterKeys { it in includeModules || it == ":" }
                .mapValues { (_, dependencies) ->
                    dependencies.filter { it in includeModules }
                }
            val referenceModules = filtered.values.flatten().toSet()
            filtered = filtered.filter { (module, dependencies) ->
                dependencies.isNotEmpty() && module in referenceModules
            }
            filtered.forEach { (moduleName, dependencies) ->
                dependencies.forEach { dependencyGraph.addEdge(moduleName, it) }
            }
        }
        return dependencyGraph
    }

    private fun parseSettingsDeps(settingsText: String): List<String> {
        return includeRegex.findAll(settingsText)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun parseModuleDeps(buildText: String): List<Pair<String, String>> {
        return dependencyRegex.findAll(buildText)
            .map {
                val scope = it.groupValues[1] // implementation
                val module = it.groupValues[2]
                scope to module
            }.toList()
    }

    private fun moduleNameFromPath(relativePath: String): String {
        val parentDir = relativePath.substringBeforeLast('/', "")
        return if (parentDir.isEmpty()) ":" else ":" + parentDir.replace('/', ':')
    }

    companion object {
        private val includeRegex = Regex("""include\s*\(["'](:[^"']+)["']\s*\)""")
        private val dependencyRegex = Regex(
            """(\w+)\s*\(\s*project\s*\(\s*["'](:[^"']+)["']\s*\)\s*\)""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )
    }
}
