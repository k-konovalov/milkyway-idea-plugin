package com.github.milkyway.idea

import com.github.milkyway.core.analyzer.ArticulationPointsAnalyzer
import com.github.milkyway.core.shape.GraphShapeMatcher
import com.github.milkyway.idea.cytoscape.ReportBuilder
import com.github.milkyway.idea.resolver.GradleDependencyResolver
import com.github.milkyway.idea.resolver.RegexDependencyResolver
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
        println("Gradle Traverse started")
//        val gradleDependencyResolver = GradleDependencyResolver(ideaProject, projectDir)
//        var graph = gradleDependencyResolver.resolve()
//        graph.adjacency.forEach { (module, children) ->
//            println("${module}: [${children}]")
//        }

        println("Regext Traverse Started")
        val regexDependencyResolver = RegexDependencyResolver(ideaProject)
        val graph = regexDependencyResolver.resolve()
        graph.adjacency.forEach { (module, children) ->
            println("${module}: [${children}]")
        }
        val articulationPointsAnalyzer = ArticulationPointsAnalyzer(graph)
        val articulationPoints = articulationPointsAnalyzer.findArticulationPoints()
        println("Articulation points: $articulationPoints")
        val graphShapeMatcher = GraphShapeMatcher()
        val matchResult = graphShapeMatcher.calculate(graph)
        println("Match Result: $matchResult")

        val cytoscapeReport = ReportBuilder().build(graph)

        return json.encodeToString(cytoscapeReport)
    }
}