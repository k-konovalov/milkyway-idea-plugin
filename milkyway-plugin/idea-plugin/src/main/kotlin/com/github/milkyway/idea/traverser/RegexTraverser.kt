package com.github.milkyway.idea.traverser

import com.github.milkyway.core.contract.traverser.DependencyTraverser
import com.intellij.openapi.project.Project
import com.github.milkyway.core.models.DependencyGraph
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

class RegexTraverser(
    private val project: Project
): DependencyTraverser {
    companion object {
        private val includeRegex = Regex("""include\s*\(["'](:[^"']+)["']\s*\)""")
        private val dependencyRegex = Regex(
            """(\w+)\s*\(\s*project\s*\(\s*["'](:[^"']+)["']\s*\)\s*\)""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )
    }

    override fun traverse(): DependencyGraph {
        val dependencyGraph = ReadAction.compute<DependencyGraph, Throwable> {
            buildDependencyGraph(project)
        }
        return dependencyGraph
    }

    private fun buildDependencyGraph(project: Project): DependencyGraph {
        val dependencyGraph = DependencyGraph()
        val result = mutableMapOf<String, MutableList<String>>()
        val settingsFile = findSettingsFile(project)
        val includeModules = settingsFile
            ?.readText()
            ?.let {parseSettingsDeps(it).toSet()}
            ?: emptySet()

        val gradleFiles = findGradleFiles(project)
        for (file in gradleFiles) {
            val text = file.readText()
            val moduleName = moduleNameFromFile(project, file).removePrefix(":")
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

    private fun findSettingsFile(project: Project): VirtualFile? {
        return project.baseDir.findChild("settings.gradle.kts")
    }

    private fun findGradleFiles(project: Project): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        VfsUtilCore.visitChildrenRecursively(project.baseDir, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.name == "build.gradle.kts" &&
                    !file.path.contains("/build/") &&
                    !file.path.contains("/.gradle/")) {
                    result.add(file)
                }
                return true
            }
        })
        return result
    }

    private fun VirtualFile.readText(): String {
        return String(this.contentsToByteArray(), Charsets.UTF_8)
    }

    private  fun parseSettingsDeps(settingsText: String): List<String> {
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

    private fun moduleNameFromFile(project: Project, file: VirtualFile): String {
        val relativePath = VfsUtilCore.getRelativePath(file.parent, project.baseDir) ?: return ""
        return ":" + relativePath.replace("/", ":")
    }
}