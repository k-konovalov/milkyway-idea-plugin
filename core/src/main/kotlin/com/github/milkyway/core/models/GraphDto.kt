package com.github.milkyway.core.models

import kotlinx.serialization.Serializable

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
