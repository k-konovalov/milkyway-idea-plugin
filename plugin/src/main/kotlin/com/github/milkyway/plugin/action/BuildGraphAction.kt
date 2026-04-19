package com.github.milkyway.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.github.milkyway.plugin.services.MyProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

class BuildGraphAction : AnAction() {
    private val GradleProjectId = ProjectSystemId("GRADLE");
    private val includeRegex = Regex("""include\s*\(["'](:[^"']+)["']\s*\)""")
    private val dependencyRegex = Regex(
        """(\w+)\s*\(\s*project\s*\(\s*["'](:[^"']+)["']\s*\)\s*\)""",
        setOf(RegexOption.DOT_MATCHES_ALL)
    )

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project: Project = actionEvent.project ?: return
        val service = project.service<MyProjectService>()

        ApplicationManager.getApplication().executeOnPooledThread {
            val graph = ReadAction.compute<Map<String, List<String>>, Throwable> {
                buildGraph(project)
            }

            val dot = toDot(graph)
            saveToDot(project, dot)

            ApplicationManager.getApplication().invokeLater {
                println("before build graph")
                graph.forEach { (module, dependencies) ->
                    println("$module -> $dependencies")
                }
            }
        }
        println("After build graph")
    }

    fun saveToDot(project: Project, content: String) {
        val settingsFile = findSettingsFile(project)
        val targetDir = if (settingsFile != null) {
            settingsFile.parent
        } else {
            project.baseDir
        } ?: return

        val file = java.io.File(targetDir.path, "dependencies.dot")
        file.writeText(content)
    }

    fun toDot(graph: Map<String, List<String>>): String {
        val builder = StringBuilder()
        builder.appendLine("digraph G {")
        builder.appendLine("    rankdir=LR;")
        builder.appendLine("    node [shape=box];")

        graph.keys.forEach { module ->
            builder.appendLine("""    "$module" [style=filled, fillcolor=${moduleColor(module)}];""")
        }

        graph.forEach { (module, dependencies) ->
            if (dependencies.isEmpty()) {
                builder.appendLine("""    "$module";""")
            } else {
                dependencies.forEach { dependency ->
                    builder.appendLine("""    "$module" -> "$dependency";""")
                }
            }
        }
        builder.appendLine("}")
        return builder.toString()
    }

    fun moduleColor(module: String): String = when {
        module.startsWith(":feature") -> "lightblue"
        module.startsWith(":core") -> "lightgray"
        module.startsWith(":model") -> "lightyellow"
        else -> "white"
    }

    fun buildGraph(project: Project): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        val settingsFile = findSettingsFile(project)
        val includeModules = settingsFile
            ?.readText()
            ?.let {parseSettingsDeps(it).toSet()}
            ?: emptySet()

        val gradleFiles = findGradleFiles(project)
        for (file in gradleFiles) {
            val text = file.readText()
            val moduleName = moduleNameFromFile(project, file)
            val dependencies = parseModuleDeps(text).map { it.second }
            result.computeIfAbsent(moduleName) { mutableListOf() }
                .addAll(dependencies)
        }
        if (includeModules.isNotEmpty()) {
            val filtered = result
                .filterKeys { it in includeModules || it == ":" }
                .mapValues { (_, dependencies) ->
                    dependencies.filter { it in includeModules }
                }
            val referenceModules = filtered.values.flatten().toSet()
            return filtered.filter { (module, dependencies) ->
                dependencies.isNotEmpty() && module in referenceModules
            }
        }
        return result
    }

    fun normalizeModuleNames(module: String): String {
        return module.removePrefix(":")
    }

    fun findSettingsFile(project: Project): VirtualFile? {
        return project.baseDir.findChild("settings.gradle.kts")
    }

    fun findGradleFiles(project: Project): List<VirtualFile> {
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

    fun VirtualFile.readText(): String {
        return String(this.contentsToByteArray(), Charsets.UTF_8)
    }

    fun parseSettingsDeps(settingsText: String): List<String> {
        return includeRegex.findAll(settingsText)
            .map { it.groupValues[1] }
            .toList()
    }

    fun parseModuleDeps(buildText: String): List<Pair<String, String>> {
        return dependencyRegex.findAll(buildText)
            .map {
                val scope = it.groupValues[1] // implementation
                val module = it.groupValues[2]
                scope to module
            }.toList()
    }

    fun moduleNameFromFile(project: Project, file: VirtualFile): String {
        val relativePath = VfsUtilCore.getRelativePath(file.parent, project.baseDir) ?: return ""
        return ":" + relativePath.replace("/", ":")
    }
}
