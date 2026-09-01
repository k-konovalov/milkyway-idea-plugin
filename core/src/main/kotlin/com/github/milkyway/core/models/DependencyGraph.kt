package com.github.milkyway.core.models

class DependencyGraph {

    val adjacency: MutableMap<Node, MutableSet<Node>> = linkedMapOf()

    val nodes: Set<Node>
        get() = adjacency.keys

    fun addNode(node: Node) {
        adjacency.computeIfAbsent(node) { linkedSetOf() }
    }

    fun addEdge(from: Node, to: Node) {
        addNode(from)
        addNode(to)
        adjacency.getValue(from).add(to)
    }

    fun addEdge(fromId: String, toId: String) {
        val from = Node(fromId)
        val to = Node(toId)
        addEdge(from, to)
    }

    fun edgeCount(): Int {
        return adjacency.values.sumOf { it.size }
    }

}
