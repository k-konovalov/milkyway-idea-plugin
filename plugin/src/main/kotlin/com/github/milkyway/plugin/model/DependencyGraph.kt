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

    fun moduleColor(module: String): String = when {
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
}