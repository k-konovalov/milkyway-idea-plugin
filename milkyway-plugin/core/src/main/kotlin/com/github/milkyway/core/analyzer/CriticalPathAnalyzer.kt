package com.github.milkyway.core.analyzer

import com.github.milkyway.core.models.CondensedGraph
import com.github.milkyway.core.models.CriticalPathsResult
import com.github.milkyway.core.models.DependencyGraph

/**
 * Analyzes critical paths in a dependency graph.
 *
 * Algorithm:
 * 1. Finds strongly connected components (SCCs) using Tarjan's algorithm.
 * 2. Builds a condensed graph (DAG) from the SCCs.
 * 3. Finds the longest paths in the DAG using topological sorting.
 *
 * @see DependencyGraph
 * @see CriticalPathsResult
 */
class CriticalPathAnalyzer {

    private companion object {
        const val INITIAL_PATH_LENGTH = 1
        const val UNREACHABLE_DISTANCE = Int.MIN_VALUE
    }

    fun findCriticalPaths(graph: DependencyGraph): CriticalPathsResult {
        return graph
            .let(::condenseGraph)
            .let(::findLongestPathsInCondensedGraph)
    }

    private fun condenseGraph(graph: DependencyGraph): CondensedGraph {
        val sccComponents = TarjanSccFinder(graph).find().sortedBy { it.id }
        val nodeToComponentId = mutableMapOf<String, Int>()

        for (component in sccComponents) {
            for (node in component.nodes) {
                nodeToComponentId[node] = component.id
            }
        }

        val condensedAdjacency = linkedMapOf<Int, MutableSet<Int>>()

        for (component in sccComponents) {
            condensedAdjacency.computeIfAbsent(component.id) { linkedSetOf() }
        }

        graph.adjacency.forEach { (from, targets) ->
            val fromComponent = nodeToComponentId.getValue(from)

            targets.forEach { to ->
                val toComponent = nodeToComponentId.getValue(to)

                if (fromComponent != toComponent) {
                    condensedAdjacency.getValue(fromComponent).add(toComponent)
                }
            }
        }

        return CondensedGraph(sccComponents, condensedAdjacency, nodeToComponentId)
    }

    private fun findLongestPathsInCondensedGraph(
        condensedGraph: CondensedGraph,
    ): CriticalPathsResult {
        val adjacency = condensedGraph.adjacency
        val allNodes = condensedGraph.components.map { it.id }.toSet()

        val indegree = allNodes
            .associateWith { 0 }
            .toMutableMap()

        adjacency.values.flatten().forEach { target ->
            indegree[target] = indegree.getValue(target) + 1
        }

        val roots = indegree
            .filterValues { it == 0 }
            .keys
            .sorted()

        val topoIndegree = indegree.toMutableMap()
        val queue = ArrayDeque(roots)
        val topoOrder = mutableListOf<Int>()

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            topoOrder += current

            adjacency[current].orEmpty().forEach { neighbor ->
                topoIndegree[neighbor] = topoIndegree.getValue(neighbor) - 1

                if (topoIndegree.getValue(neighbor) == 0) {
                    queue.addLast(neighbor)
                }
            }
        }

        val distance = allNodes
            .associateWith { UNREACHABLE_DISTANCE }
            .toMutableMap()

        val predecessors = allNodes
            .associateWith { linkedSetOf<Int>() }
            .toMutableMap()

        roots.forEach { root ->
            distance[root] = INITIAL_PATH_LENGTH
        }

        topoOrder.forEach { node ->
            val currentDistance = distance.getValue(node)

            if (currentDistance == UNREACHABLE_DISTANCE) {
                return@forEach
            }

            adjacency[node].orEmpty().forEach { neighbor ->
                val candidate = currentDistance + 1
                val existing = distance.getValue(neighbor)

                when {
                    candidate > existing -> {
                        distance[neighbor] = candidate
                        predecessors.getValue(neighbor).clear()
                        predecessors.getValue(neighbor).add(node)
                    }

                    candidate == existing -> {
                        predecessors.getValue(neighbor).add(node)
                    }
                }
            }
        }

        val longestLength = distance.values
            .maxOrNull()
            ?.takeIf { it != UNREACHABLE_DISTANCE }
            ?: 0

        val endNodes = allNodes
            .filter { distance.getValue(it) == longestLength }
            .sortedWith { a, b ->
                compareComponentsLexicographically(a, b, condensedGraph)
            }

        val componentPaths = endNodes
            .flatMap { endNode ->
                restoreAllPaths(endNode, predecessors)
            }
            .sortedWith { left, right ->
                compareComponentPaths(left, right, condensedGraph)
            }

        val componentsById = condensedGraph.components.associateBy { it.id }

        val expandedPaths = componentPaths.map { path ->
            path.map { componentId ->
                componentsById.getValue(componentId).nodes
            }
        }

        return CriticalPathsResult(
            longestPathLength = longestLength,
            componentPaths = componentPaths,
            expandedPaths = expandedPaths,
            condensedGraph = condensedGraph,
        )
    }

    private fun restoreAllPaths(endNode: Int, predecessors: Map<Int, Set<Int>>): List<List<Int>> {
        val previousNodes = predecessors[endNode].orEmpty()

        if (previousNodes.isEmpty()) {
            return listOf(listOf(endNode))
        }

        return previousNodes.flatMap { predecessor ->
            restoreAllPaths(predecessor, predecessors).map { path ->
                path + endNode
            }
        }
    }

    private fun compareComponentPaths(left: List<Int>, right: List<Int>, condensedGraph: CondensedGraph): Int {
        val minSize = minOf(left.size, right.size)

        for (index in 0 until minSize) {
            val comparison = compareComponentsLexicographically(left[index], right[index], condensedGraph)

            if (comparison != 0) {
                return comparison
            }
        }

        return left.size.compareTo(right.size)
    }

    private fun compareComponentsLexicographically(left: Int, right: Int, condensedGraph: CondensedGraph): Int {
        fun componentLabel(id: Int): String {
            return condensedGraph.components
                .first { it.id == id }
                .nodes
                .sorted()
                .joinToString("|")
        }

        return componentLabel(left).compareTo(componentLabel(right))
    }
}