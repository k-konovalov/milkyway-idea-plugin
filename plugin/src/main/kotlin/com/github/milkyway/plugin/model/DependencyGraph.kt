package com.github.milkyway.plugin.model

class DependencyGraph() {
    private val adj: MutableMap<Node, MutableSet<Node>> = mutableMapOf()

    private val nodes = mutableMapOf<String, Node>()

    private var articulationPoints = mutableListOf<Node>()

    private fun getNode(name: String): Node = nodes.getOrPut(name) { Node(name) }

    fun addEdge(fromName: String, toName: String) {
        val from = getNode(fromName)
        val to = getNode(toName)
        adj.getOrPut(from) { mutableSetOf() }.add(to)
    }

    fun getRawEdges(): List<Pair<String, String>> {
        val edges = mutableListOf<Pair<String, String>>()
        adj.forEach { (node, children) ->
            run {
                for (child in children) {
                    edges.add(Pair(node.name, child.name))
                }
            }
        }
        return edges
    }

    fun toDot(): String {
        val builder = StringBuilder()
        builder.appendLine("digraph G {")
        builder.appendLine("    rankdir=LR;")
        builder.appendLine("    node [shape=box];")

        val rawEdges = getRawEdges()
        nodes.keys.forEach { module ->
            if (Node(module) in articulationPoints) {
                builder.appendLine("""    "$module" [style=filled, fillcolor=red];""")
            } else {
                builder.appendLine("""    "$module" [style=filled, fillcolor=${moduleColor(module)}];""")
            }
        }

        rawEdges.forEach { (from, to) ->
            builder.appendLine("""    "$from" -> "$to";""")
        }
        builder.appendLine("}")
        return builder.toString()
    }

    private fun moduleColor(module: String): String = when {
        module.startsWith(":feature") -> "lightblue"
        module.startsWith(":core") -> "lightgray"
        module.startsWith(":model") -> "lightyellow"
        else -> "white"
    }

    fun computeArticulationPoints() {
        val directRawEdges = getRawEdges()
        val indirectRawEdges = directRawEdges + directRawEdges.map {(a, b) -> b to a}
        val articulationPointsGraph = ArticulationPointsGraph(indirectRawEdges)
        articulationPoints = articulationPointsGraph.findArticulationPoints().toMutableList()
        print(articulationPoints)
    }

    fun buildLayerProfile(): List<Int> {
        val indegree = mutableMapOf<Node, Int>().withDefault { 0 }

        for (node in nodes.values) {
            indegree.putIfAbsent(node, 0)
        }

        for ((_, children) in adj) {
            for (child in children) {
                indegree[child] = indegree.getValue(child) + 1
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
            for (to in adj[from].orEmpty()) {
                val newLayer = layer.getValue(from) + 1
                layer[to] = maxOf(layer.getOrDefault(to, 0), newLayer)
                indegree[to] = indegree.getValue(to) - 1;
                if (indegree[to] == 0) {
                    queue.add(to)
                }
            }
        }
        if (processed != nodes.size) {
            error("Graph contains cycles. Expected DAG")
        }
        // endregion
        val counts = mutableMapOf<Int, Int>().withDefault { 0 }
        for (node in nodes.values) {
            val l = layer.getValue(node)
            counts[l] = counts.getValue(l) + 1
        }

        val maxLayer = counts.keys.maxOrNull() ?: 0
        return List(maxLayer + 1) { i ->
            counts.getOrDefault(i, 0)
        }
    }
}