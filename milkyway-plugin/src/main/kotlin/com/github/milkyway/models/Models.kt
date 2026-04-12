package com.github.milkyway.models

data class EdgeVisit(
    val from: String,
    val to: String,
)

class DependencyGraph {

    val adjacency: MutableMap<String, MutableSet<String>> = linkedMapOf()

    fun addNode(node: String) {
        adjacency.computeIfAbsent(node) { linkedSetOf() }
    }

    fun addEdge(from: String, to: String) {
        addNode(from)
        addNode(to)
        adjacency.getValue(from).add(to)
    }

    fun nodeCount(): Int {
        return adjacency.size
    }

    fun edgeCount(): Int {
        return adjacency.values.sumOf { it.size }
    }

}

data class StronglyConnectedComponent(
    val id: Int,
    val nodes: Set<String>,
)

data class CondensedGraph(
    val components: List<StronglyConnectedComponent>,
    val adjacency: Map<Int, Set<Int>>,
    val nodeToComponentId: Map<String, Int>,
)

data class CriticalPathsResult(
    val longestPathLength: Int,
    val componentPaths: List<List<Int>>,
    val expandedPaths: List<List<Set<String>>>,
    val condensedGraph: CondensedGraph,
)
