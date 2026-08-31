package com.github.milkyway.algorithm.shapematching

import com.github.milkyway.algorithm.api.GraphAnalyzer
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node

class GraphShapeMatcher : GraphAnalyzer<ShapeMatchResult> {

    private val shapeMatcher = ShapeMatcher()

    override suspend fun analyze(graph: DependencyGraph): ShapeMatchResult {
        val dag = graph.asAcyclic()
        val acyclicGraphProfile = buildLayerProfile(dag)
        return ShapeMatchResult(shapeMatcher.calculate(acyclicGraphProfile))
    }

    private fun buildLayerProfile(graph: DependencyGraph): List<Int> {
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

        // tracks out-degree: nodes with 0 outgoing edges (sinks) start the reverse traversal
        for ((from, children) in graph.adjacency) {
            for (to in children) {
                indegree[from] = indegree.getValue(from) + 1
            }
        }

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
                indegree[to] = indegree.getValue(to) - 1
                if (indegree[to] == 0) {
                    queue.add(to)
                }
            }
        }

        if (processed != graph.nodes.size) {
            error("Graph contains cycles. Expected DAG")
        }

        val counts = mutableMapOf<Int, Int>().withDefault { 0 }
        for (node in graph.nodes) {
            val l = layer.getValue(node)
            counts[l] = counts.getValue(l) + 1
        }

        val maxLayer = counts.keys.maxOrNull() ?: 0
        val reversedProfile = List(maxLayer + 1) { i -> counts.getOrDefault(i, 0) }
        return reversedProfile.reversed()
    }

    private fun DependencyGraph.asAcyclic(): DependencyGraph {
        val result = DependencyGraph()

        for (node in nodes) {
            result.addNode(node)
        }

        val state = mutableMapOf<Node, VisitState>()

        fun dfs(node: Node) {
            state[node] = VisitState.GRAY

            val targets = adjacency[node]?.toList().orEmpty()
            for (next in targets) {
                when (state[next] ?: VisitState.WHITE) {
                    VisitState.WHITE -> {
                        result.addEdge(node, next)
                        dfs(next)
                    }
                    VisitState.GRAY -> { /* skip back edge to break cycle */ }
                    VisitState.BLACK -> {
                        result.addEdge(node, next)
                    }
                }
            }

            state[node] = VisitState.BLACK
        }

        for (node in nodes) {
            if ((state[node] ?: VisitState.WHITE) == VisitState.WHITE) {
                dfs(node)
            }
        }

        return result
    }

    private enum class VisitState { WHITE, GRAY, BLACK }
}
