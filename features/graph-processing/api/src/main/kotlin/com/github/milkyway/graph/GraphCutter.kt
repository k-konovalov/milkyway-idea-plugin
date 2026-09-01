package com.github.milkyway.graph

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node

class GraphCutter(
    private val dependencyGraph: DependencyGraph,
    private val moduleName: String? = null,
) {
    fun cut(): DependencyGraph {
        if (moduleName == null) {
            return dependencyGraph
        }
        return dependencyGraph.startFromModuleName(moduleName)
    }

    private fun DependencyGraph.startFromModuleName(moduleName: String): DependencyGraph {
        val srcNode = Node(moduleName)
        if (srcNode !in adjacency.keys) {
            return this
        }

        val queue = ArrayDeque<Node>()
        queue.add(srcNode)

        val shrunkGraph = DependencyGraph()
        val visited = mutableSetOf<Node>()

        while (queue.isNotEmpty()) {
            val from = queue.removeFirst()
            if (from in visited) {
                continue
            }
            visited.add(from)
            for (to in adjacency.getValue(from)) {
                shrunkGraph.addEdge(from, to)
                queue.add(to)
            }
        }
        return shrunkGraph
    }
}
