package com.github.milkyway.algorithm.criticalpath

import com.github.milkyway.algorithm.api.GraphAnalyzer
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node

class CriticalPathAnalyzer : GraphAnalyzer<CriticalPathResult> {

    override suspend fun analyze(graph: DependencyGraph): CriticalPathResult =
        findCriticalPaths(graph)

    private fun findCriticalPaths(graph: DependencyGraph): CriticalPathResult {
        val condensedGraph = condenseGraph(graph)
        return findLongestPathsInCondensedGraph(condensedGraph, graph)
    }

    private fun condenseGraph(graph: DependencyGraph): CondensedGraph {
        val components = TarjanSccFinder(graph).find()
        val nodeToComponentId = mutableMapOf<Node, Int>()
        for (component in components) {
            for (node in component.nodes) {
                nodeToComponentId[node] = component.id
            }
        }

        val adjacency = mutableMapOf<Int, MutableSet<Int>>()
        for (component in components) {
            adjacency[component.id] = mutableSetOf()
        }

        for ((from, targets) in graph.adjacency) {
            val fromComponentId = nodeToComponentId.getValue(from)
            for (to in targets) {
                val toComponentId = nodeToComponentId.getValue(to)
                if (fromComponentId != toComponentId) {
                    adjacency.getValue(fromComponentId).add(toComponentId)
                }
            }
        }

        return CondensedGraph(
            components = components,
            adjacency = adjacency,
            nodeToComponentId = nodeToComponentId,
        )
    }

    private fun findLongestPathsInCondensedGraph(
        condensedGraph: CondensedGraph,
        originalGraph: DependencyGraph,
    ): CriticalPathResult {
        val dist = mutableMapOf<Int, Int>()
        val prev = mutableMapOf<Int, Int?>()

        for (component in condensedGraph.components) {
            dist[component.id] = UNREACHABLE_DISTANCE
            prev[component.id] = null
        }

        val topologicalOrder = topologicalSort(condensedGraph)

        for (nodeId in topologicalOrder) {
            if (dist.getValue(nodeId) == UNREACHABLE_DISTANCE) {
                dist[nodeId] = INITIAL_PATH_LENGTH
            }

            val neighbors = condensedGraph.adjacency[nodeId].orEmpty()
            val sortedNeighbors = neighbors.sortedWith { left, right ->
                compareComponentsLexicographically(left, right, condensedGraph)
            }

            for (neighborId in sortedNeighbors) {
                val newDist = dist.getValue(nodeId) + 1
                if (newDist > dist.getValue(neighborId) ||
                    (newDist == dist.getValue(neighborId) &&
                            compareComponentPaths(nodeId, prev.getValue(neighborId), condensedGraph) < 0)
                ) {
                    dist[neighborId] = newDist
                    prev[neighborId] = nodeId
                }
            }
        }

        val longestPathLength = dist.values.maxOrNull() ?: 0

        val endNodes = dist.entries
            .filter { (_, d) -> d == longestPathLength }
            .map { (id, _) -> id }
            .sortedWith { left, right ->
                compareComponentsLexicographically(left, right, condensedGraph)
            }

        val componentPaths = endNodes.map { endNode ->
            val path = mutableListOf<Int>()
            var current: Int? = endNode
            while (current != null) {
                path.add(current)
                current = prev[current]
            }
            path.reversed()
        }

        val expandedPaths = componentPaths.map { path ->
            path.map { componentId ->
                condensedGraph.components.first { it.id == componentId }.nodes
            }
        }

        return CriticalPathResult(
            longestPathLength = longestPathLength,
            expandedPaths = expandedPaths,
        )
    }

    private fun topologicalSort(condensedGraph: CondensedGraph): List<Int> {
        val visited = mutableSetOf<Int>()
        val result = mutableListOf<Int>()

        fun dfs(nodeId: Int) {
            if (nodeId in visited) return
            visited.add(nodeId)
            for (neighbor in condensedGraph.adjacency[nodeId].orEmpty()) {
                dfs(neighbor)
            }
            result.add(nodeId)
        }

        for (component in condensedGraph.components.sortedBy { it.id }) {
            dfs(component.id)
        }

        return result.reversed()
    }

    private fun compareComponentPaths(
        left: Int?,
        right: Int?,
        condensedGraph: CondensedGraph,
    ): Int {
        if (left == null && right == null) return 0
        if (left == null) return 1
        if (right == null) return -1
        return compareComponentsLexicographically(left, right, condensedGraph)
    }

    private fun compareComponentsLexicographically(
        left: Int,
        right: Int,
        condensedGraph: CondensedGraph,
    ): Int {
        fun componentLabel(id: Int): String {
            return condensedGraph.components
                .first { it.id == id }
                .nodes
                .sortedBy { it.id }
                .joinToString("|") { it.id }
        }

        return componentLabel(left).compareTo(componentLabel(right))
    }

    private companion object {
        const val INITIAL_PATH_LENGTH = 0
        const val UNREACHABLE_DISTANCE = -1
    }
}
