package com.github.milkyway.tasks

import com.github.milkyway.analyzer.CriticalPathAnalyzer
import com.github.milkyway.exporters.GraphvizExporter
import com.github.milkyway.models.CriticalPathsResult
import com.github.milkyway.models.DependencyGraph
import com.github.milkyway.traversers.ProjectModulesTraverser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PrintDependenciesTask : DefaultTask() {

    @TaskAction
    fun printAll() {
        val graph = DependencyGraph()
        val traverser = ProjectModulesTraverser()

        for (subproject in project.rootProject.allprojects) {
            traverser.traverse(subproject, graph)
        }

        val analyzer = CriticalPathAnalyzer()
        val result = analyzer.findCriticalPaths(graph)

        printGraph(graph)
        printAnalyzerResult(result)
        exportToGraphviz(result, graph)
    }

    private fun exportToGraphviz(result: CriticalPathsResult, graph: DependencyGraph) {
        val criticalPathNodes = result.expandedPaths
            .firstOrNull()
            ?.mapNotNull { componentNodes -> componentNodes.firstOrNull() }
            .orEmpty()

        val exporter = GraphvizExporter()
        val outputDir = project.layout.buildDirectory.dir("reports/MilkyWay").get().asFile
        exporter.exportGraph(outputDir, graph, criticalPathNodes)
    }

    fun printAnalyzerResult(result: CriticalPathsResult) {
        println("")
        println("Critical path info:")
        println("Condensed graph nodes: ${result.condensedGraph.components.size}")
        println("Critical path length: ${result.longestPathLength}")
        println("Critical paths count: ${result.componentPaths.size}")
        println("")

        result.expandedPaths.forEachIndexed { index, path ->
            println("Critical path #${index + 1}:")
            var count = 0
            for (componentNodes in path) {
                val label = componentNodes.sorted().joinToString(", ")
                count++
                println("${count}. [$label]")
            }
            println("Path length: $count")
            println("")
        }
    }

    private fun printGraph(graph: DependencyGraph) {
        println("")
        println("Graph info:")
        println("Nodes: ${graph.nodeCount()}")
        println("Edges: ${graph.edgeCount()}")
        println("")

        graph.adjacency.toSortedMap().forEach { (from, targets) ->
            if (targets.isEmpty()) {
                println("$from -> []")
            } else {
                val sortedTargets = targets.toSortedSet().joinToString(", ")
                println("$from -> [$sortedTargets]")
            }
        }
    }

}