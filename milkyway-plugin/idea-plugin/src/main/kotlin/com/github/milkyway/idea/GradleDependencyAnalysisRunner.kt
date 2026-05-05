package com.github.milkyway.idea

import com.github.milkyway.idea.cytoscape.ReportBuilder
import com.github.milkyway.idea.traverser.GradleTraverser
import com.github.milkyway.idea.traverser.RegexTraverser
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.Json
import java.io.File

class GradleDependencyAnalysisRunner(
    private val ideaProject: Project,
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun run(projectDir: File): String {
        try {
            println("Gradle Traverse started")
            val gradleTraverser = GradleTraverser(ideaProject, projectDir)
            var graph = gradleTraverser.traverse()
            graph.adjacency.forEach { (module, children) ->
                println("${module}: [${children}]")
            }

            println("Regext Traverse Started")
            val regexTraverser = RegexTraverser(ideaProject)
            graph = regexTraverser.traverse()
            graph.adjacency.forEach { (module, children) ->
                println("${module}: [${children}]")
            }

            val cytoscapeReport = ReportBuilder().build(graph)

            return json.encodeToString(cytoscapeReport)
        } finally {
            // TODO: Remove try-catch block. If Lelikut approve.
            // Do nothing. With gradle it was initScript.delete()
        }
    }
}