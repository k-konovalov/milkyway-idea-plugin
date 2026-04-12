package com.github.milkyway.exporters

import com.github.milkyway.models.DependencyGraph
import java.io.File

class GraphvizExporter {

    private fun toDot(graph: DependencyGraph, criticalPath: List<String>): String {
        val sb = StringBuilder()

        sb.appendLine("digraph DependencyGraph {")
        sb.appendLine("  rankdir=LR;ranksep=0.3;nodesep=0.3;")
        sb.appendLine("  graph [overlap=true];")
        sb.appendLine("  node [shape=box];")

        val adjacency = graph.adjacency
        val allNodes = buildSet {
            for ((from, targets) in adjacency) {
                add(from)
                for (to in targets) add(to)
            }
        }.sorted()
        val escapedNodeNames = allNodes.associateWith { it.escapeDot() }
        val criticalNodes = criticalPath.toHashSet()
        val criticalEdges = criticalPath.zipWithNext().toHashSet()

        addNodes(allNodes, escapedNodeNames, criticalNodes, sb)
        addEdges(adjacency, escapedNodeNames, criticalEdges, sb)

        sb.appendLine("}")

        return sb.toString()
    }

    private fun addEdges(
        adjacency: MutableMap<String, MutableSet<String>>,
        escapedNodeNames: Map<String, String>,
        criticalEdges: HashSet<Pair<String, String>>,
        sb: StringBuilder
    ) {
        for (from in adjacency.keys.sorted()) {
            val escapedFrom = escapedNodeNames[from] ?: continue
            val sortedTargets = adjacency[from].orEmpty().asSequence().distinct().sorted()

            for (to in sortedTargets) {
                val escapedTo = escapedNodeNames[to] ?: continue

                if ((from to to) in criticalEdges) {
                    sb.appendLine("""  "$escapedFrom" -> "$escapedTo" [color=red, penwidth=3];""")
                } else {
                    sb.appendLine("""  "$escapedFrom" -> "$escapedTo";""")
                }
            }
        }
    }

    private fun addNodes(
        allNodes: List<String>,
        escaped: Map<String, String>,
        criticalNodes: HashSet<String>,
        sb: StringBuilder
    ) {
        for (node in allNodes) {
            val escapedNode = escaped.getValue(node)

            if (node in criticalNodes) {
                sb.appendLine("""  "$escapedNode" [shape=box, style=filled, color=red, penwidth=3];""")
            } else {
                sb.appendLine("""  "$escapedNode" [shape=box];""")
            }
        }
    }

    private fun String.escapeDot(): String {
        return this.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    fun exportGraph(outputDir: File, graph: DependencyGraph, criticalPathNodes: List<String>) {
        outputDir.mkdirs()

        val outputFile = outputDir.resolve("graph.dot")
        outputFile.writeText(toDot(graph, criticalPathNodes))
    }
}