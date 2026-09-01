package com.github.milkyway.algorithm.articulationpoints

import com.github.milkyway.algorithm.api.GraphAnalyzer
import com.github.milkyway.algorithm.articulationpoints.api.ArticulationPointsResult
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.EdgeVisit
import com.github.milkyway.core.models.Node

class ArticulationPointsAnalyzer : GraphAnalyzer<ArticulationPointsResult> {

    override suspend fun analyze(graph: DependencyGraph): ArticulationPointsResult =
        ArticulationPointsResult(findArticulationPoints(graph))

    private fun findArticulationPoints(graph: DependencyGraph): Set<Node> {
        val nodes: MutableMap<String, Node> = mutableMapOf()
        val directedEdges: MutableList<EdgeVisit> = mutableListOf()
        val neighbours: MutableMap<Node, MutableList<Node>> = mutableMapOf()

        val edges = mutableListOf<Pair<String, String>>()
        graph.adjacency.forEach { (node, children) ->
            for (child in children) {
                edges.add(Pair(node.id, child.id))
            }
        }

        for ((fromName, toName) in edges) {
            val from = nodes.getOrPut(fromName) { Node(fromName) }
            val to = nodes.getOrPut(toName) { Node(toName) }
            neighbours.getOrPut(from) { mutableListOf() }.add(to)
            neighbours.getOrPut(to) { mutableListOf() }.add(from)
            directedEdges.add(EdgeVisit(from, to))
        }

        for ((node, list) in neighbours) {
            neighbours[node] = list
                .distinctBy { it.id }
                .sortedBy { it.id }
                .toMutableList()
        }

        val tin = mutableMapOf<Node, Int>()
        val low = mutableMapOf<Node, Int>()
        val visited = mutableSetOf<Node>()
        val articulation = mutableSetOf<Node>()
        var timer = 0

        for (start in nodes.values) {
            if (start in visited) continue

            // from, parent, nextNeighbourIndex
            val stack = ArrayDeque<Triple<Node, Node?, Int>>()
            val childrenCount = mutableMapOf<Node, Int>().withDefault { 0 }
            stack.addLast(Triple(start, null, 0))

            while (stack.isNotEmpty()) {
                val (from, parent, nextNeighbourIndex) = stack.removeLast()

                if (from !in visited) {
                    visited.add(from)
                    tin[from] = timer
                    low[from] = timer
                    ++timer
                }

                val adj = neighbours[from] ?: emptyList()
                if (nextNeighbourIndex < adj.size) {
                    val to = adj[nextNeighbourIndex]
                    stack.addLast(Triple(from, parent, nextNeighbourIndex + 1))

                    if (to == parent) {
                        continue
                    }
                    if (to in visited) {
                        low[from] = minOf(low[from]!!, tin[to]!!)
                    } else {
                        childrenCount[from] = childrenCount.getValue(from) + 1
                        stack.addLast(Triple(to, from, 0))
                    }
                    continue
                }

                if (parent != null) {
                    low[parent] = minOf(low[parent]!!, low[from]!!)

                    if (low[from]!! >= tin[parent]!!) {
                        articulation.add(parent)
                    }
                } else {
                    if (childrenCount.getValue(from) > 1) {
                        articulation.add(from)
                    }
                }
            }
        }

        return articulation
    }
}
