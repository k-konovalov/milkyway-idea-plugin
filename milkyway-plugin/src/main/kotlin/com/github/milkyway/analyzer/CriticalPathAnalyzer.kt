package com.github.milkyway.analyzer

import com.github.milkyway.models.CondensedGraph
import com.github.milkyway.models.CriticalPathsResult
import com.github.milkyway.models.DependencyGraph
import com.github.milkyway.models.StronglyConnectedComponent

class CriticalPathAnalyzer {

    fun findCriticalPaths(graph: DependencyGraph): CriticalPathsResult {
        val condensed = condenseGraph(graph)
        return findLongestPathsInCondensedGraph(condensed)
    }

    private fun condenseGraph(graph: DependencyGraph): CondensedGraph {
        val sccComponents = findStronglyConnectedComponents(graph).sortedBy { it.id }
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

        for ((from, targets) in graph.adjacency) {
            val fromComponent = nodeToComponentId.getValue(from)

            for (to in targets) {
                val toComponent = nodeToComponentId.getValue(to)

                if (fromComponent != toComponent) {
                    condensedAdjacency.getValue(fromComponent).add(toComponent)
                }
            }
        }

        return CondensedGraph(sccComponents, condensedAdjacency, nodeToComponentId)
    }

    private fun findStronglyConnectedComponents(graph: DependencyGraph): List<StronglyConnectedComponent> {
        var index = 0
        val indexMap = mutableMapOf<String, Int>()
        val lowLinkMap = mutableMapOf<String, Int>()
        val stack = ArrayDeque<String>()
        val onStack = mutableSetOf<String>()
        val result = mutableListOf<StronglyConnectedComponent>()

        fun strongConnect(node: String) {
            indexMap[node] = index
            lowLinkMap[node] = index
            index++

            stack.addLast(node)
            onStack.add(node)

            for (neighbor in graph.adjacency[node].orEmpty()) {
                if (neighbor !in indexMap) {
                    strongConnect(neighbor)
                    lowLinkMap[node] = minOf(lowLinkMap.getValue(node), lowLinkMap.getValue(neighbor))
                } else if (neighbor in onStack) {
                    lowLinkMap[node] = minOf(lowLinkMap.getValue(node), indexMap.getValue(neighbor))
                }
            }

            if (lowLinkMap.getValue(node) == indexMap.getValue(node)) {
                val componentNodes = linkedSetOf<String>()

                while (true) {
                    val top = stack.removeLast()
                    onStack.remove(top)
                    componentNodes.add(top)

                    if (top == node) {
                        break
                    }
                }

                result += StronglyConnectedComponent(result.size, componentNodes)
            }
        }

        for (node in graph.adjacency.keys.sorted()) {
            if (node !in indexMap) {
                strongConnect(node)
            }
        }

        return result
    }

    private fun findLongestPathsInCondensedGraph(condensedGraph: CondensedGraph): CriticalPathsResult {
        val adjacency = condensedGraph.adjacency
        val allNodes = condensedGraph.components.map { it.id }.toSet()

        val indegree = mutableMapOf<Int, Int>().apply {
            for (node in allNodes) {
                this[node] = 0
            }
        }

        for ((_, targets) in adjacency) {
            for (target in targets) {
                indegree[target] = indegree.getValue(target) + 1
            }
        }

        val queue = ArrayDeque<Int>()
        for ((node, degree) in indegree) {
            if (degree == 0) {
                queue.addLast(node)
            }
        }

        val topoOrder = mutableListOf<Int>()

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            topoOrder += current

            for (neighbor in adjacency[current].orEmpty()) {
                indegree[neighbor] = indegree.getValue(neighbor) - 1
                if (indegree.getValue(neighbor) == 0) {
                    queue.addLast(neighbor)
                }
            }
        }

        val distance = mutableMapOf<Int, Int>()

        val predecessor = mutableMapOf<Int, Int?>()

        for (node in allNodes) {
            distance[node] = Int.MIN_VALUE
            predecessor[node] = null
        }

        val roots = indegree.filterValues { it == 0 }.keys

        for (root in roots) {
            distance[root] = 1
        }

        for (node in topoOrder) {
            val currentDistance = distance.getValue(node)
            if (currentDistance == Int.MIN_VALUE) {
                continue
            }

            for (neighbor in adjacency[node].orEmpty()) {
                val candidate = currentDistance + 1
                val existing = distance.getValue(neighbor)

                if (candidate > existing) {
                    distance[neighbor] = candidate
                    predecessor[neighbor] = node
                } else if (candidate == existing) {
                    val currentPredecessor = predecessor[neighbor]

                    if (currentPredecessor == null ||
                        compareComponentIds(node, currentPredecessor, condensedGraph) < 0
                    ) {
                        predecessor[neighbor] = node
                    }
                }
            }
        }

        val longestLength = distance.values.maxOrNull()?.takeIf { it != Int.MIN_VALUE } ?: 0

        val endNode = allNodes
            .filter { distance.getValue(it) == longestLength }
            .minWithOrNull { a, b ->
                compareComponentIds(a, b, condensedGraph)
            }

        val componentPaths =
            if (endNode == null) {
                emptyList()
            } else {
                listOf(restorePath(endNode, predecessor))
            }
        val componentsById = condensedGraph.components.associateBy { it.id }

        val expandedPaths = componentPaths.map { path ->
            path.map { componentId ->
                componentsById.getValue(componentId).nodes
            }
        }

        return CriticalPathsResult(longestLength, componentPaths, expandedPaths, condensedGraph)
    }

    private fun restorePath(endNode: Int, predecessor: Map<Int, Int?>): List<Int> {
        val path = mutableListOf<Int>()
        var current: Int? = endNode

        while (current != null) {
            path += current
            current = predecessor[current]
        }

        path.reverse()
        return path
    }

    private fun compareComponentIds(left: Int, right: Int, condensedGraph: CondensedGraph): Int {
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