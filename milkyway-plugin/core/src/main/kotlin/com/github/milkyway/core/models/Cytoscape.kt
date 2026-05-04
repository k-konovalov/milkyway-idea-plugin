package com.github.milkyway.core.models

import kotlinx.serialization.Serializable


@Serializable
data class CytoscapeReportDto(
    val summary: CytoscapeSummaryDto,
    val elements: List<CytoscapeElementDto>,
    val criticalPaths: List<List<String>>,
    val groups: List<CytoscapeGroupDto>,
)

@Serializable
data class CytoscapeSummaryDto(
    val nodeCount: Int,
    val edgeCount: Int,
    val criticalPathLength: Int,
)

@Serializable
data class CytoscapeElementDto(
    val data: CytoscapeDataDto,
    val classes: String = "",
)

@Serializable
data class CytoscapeDataDto(
    val id: String,
    val label: String? = null,
    val group: String? = null,
    val parent: String? = null,
    val source: String? = null,
    val target: String? = null,
    val critical: Boolean = false,
)

@Serializable
data class CytoscapeGroupDto(
    val id: String,
    val label: String,
    val nodes: List<String>,
)