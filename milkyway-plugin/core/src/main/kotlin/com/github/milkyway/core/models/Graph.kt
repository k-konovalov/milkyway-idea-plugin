package com.github.milkyway.core.models

import kotlinx.serialization.Serializable

data class Node(
    val id: String,
    val label: String = id,
)

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

data class StronglyConnectedComponent(
    val id: Int,
    val nodes: Set<Node>,
)

data class CondensedGraph(
    val components: List<StronglyConnectedComponent>,
    val adjacency: Map<Int, Set<Int>>,
    val nodeToComponentId: Map<Node, Int>,
)

data class CriticalPathsResult(
    val longestPathLength: Int,
    val componentPaths: List<List<Int>>,
    val expandedPaths: List<List<Set<Node>>>,
    val condensedGraph: CondensedGraph,
)

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
