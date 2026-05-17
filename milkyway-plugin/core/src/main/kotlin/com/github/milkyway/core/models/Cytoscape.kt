package com.github.milkyway.core.models

import kotlinx.serialization.Serializable


@Serializable
data class CytoscapeReportDto(
    val summary: CytoscapeSummaryDto,
    val elements: List<CytoscapeElementDto>,
    val criticalPaths: List<List<String>>,
    val groups: List<CytoscapeGroupDto>,
    val shapeSimilarities: List<CytoscapeShapeSimilarityDto>,
    val cytoscapePluginSettings: CytoscapePluginSettingsDto,
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
    val isArticulationPoint: Boolean = false,
)

@Serializable
data class CytoscapeGroupDto(
    val id: String,
    val label: String,
    val nodes: List<String>,
)

@Serializable
data class CytoscapeShapeSimilarityDto(
    val shapeId: String,
    val shapeName: String,
    val similarityPercent: Double
)

@Serializable
data class CytoscapePluginSettingsDto(
    val isAnimationEnabled: Boolean = false,
    val theme: String = "Black"
)
