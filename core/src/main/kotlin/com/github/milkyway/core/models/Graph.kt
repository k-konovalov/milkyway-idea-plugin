package com.github.milkyway.core.models

import kotlinx.serialization.Serializable

data class Node(
    val id: String,
    val label: String = id,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Node) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return id
    }
}

data class EdgeVisit(
    val from: Node,
    val to: Node,
)

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

@Serializable
data class DependencyGraphDto(
    val nodes: List<NodeDto>,
    val edges: List<EdgeDto>,
)

@Serializable
data class NodeDto(
    val id: String,
    val label: String = id,
)

@Serializable
data class EdgeDto(
    val from: String,
    val to: String,
)
