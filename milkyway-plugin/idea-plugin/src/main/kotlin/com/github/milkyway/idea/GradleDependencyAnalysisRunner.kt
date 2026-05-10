package com.github.milkyway.idea

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.idea.cytoscape.ReportBuilder
import com.github.milkyway.idea.resolver.DependencyResolver
import com.github.milkyway.idea.resolver.GradleDependencyResolver
import com.github.milkyway.idea.resolver.RegexDependencyResolver
import com.github.milkyway.idea.settings.MilkyWaySettings
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.Json
import java.io.File

class GradleDependencyAnalysisRunner(
    private val ideaProject: Project,
    private val settings: MilkyWaySettings = MilkyWaySettings.getInstance(),
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun run(projectDir: File): String {
        println("Gradle Traverse started")
        val isGradleParser = settings.state.parser == MilkyWaySettings.PARSER_GRADLE
        println("settings.state ${settings.state}")
        val dependencyResolver: DependencyResolver = if (isGradleParser) {
            println("Parsing by gradle")
            GradleDependencyResolver(ideaProject, projectDir)
        } else {
            println("Paring by regex")
            RegexDependencyResolver(ideaProject)
        }
        val graph: DependencyGraph = dependencyResolver.resolve()
        graph.adjacency.forEach { (module, children) ->
            println("${module}: [${children}]")
        }

        val cytoscapeReport = ReportBuilder().build(graph)

        return json.encodeToString(cytoscapeReport)
    }
}