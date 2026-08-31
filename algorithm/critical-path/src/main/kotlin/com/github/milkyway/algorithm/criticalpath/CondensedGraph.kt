package com.github.milkyway.algorithm.criticalpath

import com.github.milkyway.core.models.Node

internal data class StronglyConnectedComponent(
    val id: Int,
    val nodes: Set<Node>,
)

internal data class CondensedGraph(
    val components: List<StronglyConnectedComponent>,
    val adjacency: Map<Int, Set<Int>>,
    val nodeToComponentId: Map<Node, Int>,
)
