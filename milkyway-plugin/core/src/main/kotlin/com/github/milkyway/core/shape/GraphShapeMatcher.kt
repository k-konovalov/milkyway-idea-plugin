package com.github.milkyway.core.shape

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node
import com.github.milkyway.core.models.Shape
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.orEmpty

class GraphShapeMatcher(
    private val shapeMatcher: ShapeMatcher = ShapeMatcher()
) {
    fun calculate(graph: DependencyGraph): Map<Shape, Double> {
        val graphProfile = buildLayerProfile(graph)
        println("--------------")
        println("--------------")
        println("Graph Profile: ")
        println(graphProfile)
        val report = shapeMatcher.calculate(graphProfile)
        return report
    }

    fun buildLayerProfile(graph: DependencyGraph): List<Int> {
        val indegree = mutableMapOf<Node, Int>().withDefault { 0 }

        val reverseAdj = mutableMapOf<Node, MutableSet<Node>>()

        for ((from, children) in graph.adjacency) {
            for (to in children) {
                reverseAdj.getOrPut(to) { mutableSetOf() }.add(from)
            }
        }

        for (node in graph.nodes) {
            indegree.putIfAbsent(node, 0)
        }

        for ((from, children) in graph.adjacency) {
            for (to in children) {
                indegree[from] = indegree.getValue(from) + 1
            }
        }

        // region Topological Traversal
        val queue = ArrayDeque<Node>()
        val layer = mutableMapOf<Node, Int>()

        for ((node, deg) in indegree) {
            if (deg == 0) {
                queue.add(node)
                layer[node] = 0
            }
        }
        var processed = 0
        while (queue.isNotEmpty()) {
            val from = queue.removeFirst()
            ++processed
            for (to in reverseAdj[from].orEmpty()) {
                val newLayer = layer.getValue(from) + 1
                layer[to] = maxOf(layer.getOrDefault(to, 0), newLayer)
                indegree[to] = indegree.getValue(to) - 1;
                if (indegree[to] == 0) {
                    queue.add(to)
                }
            }
        }
        if (processed != graph.nodes.size) {
            error("Graph contains cycles. Expected DAG")
        }
        // endregion
        val counts = mutableMapOf<Int, Int>().withDefault { 0 }
        for (node in graph.nodes) {
            val l = layer.getValue(node)
            counts[l] = counts.getValue(l) + 1
        }

        val maxLayer = counts.keys.maxOrNull() ?: 0
        val reversedProfile = List(maxLayer + 1) { i ->
            counts.getOrDefault(i, 0)
        }
        return reversedProfile.reversed()
    }
}
