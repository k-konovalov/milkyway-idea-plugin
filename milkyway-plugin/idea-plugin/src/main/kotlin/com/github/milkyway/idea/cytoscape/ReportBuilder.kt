package com.github.milkyway.idea.cytoscape

import com.github.milkyway.core.analyzer.ArticulationPointsAnalyzer
import com.github.milkyway.core.analyzer.CriticalPathAnalyzer
import com.github.milkyway.core.models.CriticalPathsResult
import com.github.milkyway.core.models.CytoscapeDataDto
import com.github.milkyway.core.models.CytoscapeElementDto
import com.github.milkyway.core.models.CytoscapeGroupDto
import com.github.milkyway.core.models.CytoscapePluginSettingsDto
import com.github.milkyway.core.models.CytoscapeReportDto
import com.github.milkyway.core.models.CytoscapeShapeSimilarityDto
import com.github.milkyway.core.models.CytoscapeSummaryDto
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node
import com.github.milkyway.core.models.Shape
import com.github.milkyway.core.shape.GraphShapeMatcher
import com.github.milkyway.idea.settings.MilkyWaySettings

class ReportBuilder {

    fun build(graph: DependencyGraph): CytoscapeReportDto {
        val analyzer = CriticalPathAnalyzer()
        // Critical Path
        val criticalPathResult = analyzer.findCriticalPaths(graph)
        // Articulation Points
        val articulationPointsAnalyzer = ArticulationPointsAnalyzer(graph)
        val articulationPoints = articulationPointsAnalyzer.findArticulationPoints()
        // Shape Match
        val graphShapeMatcher = GraphShapeMatcher()
        val matchResult = graphShapeMatcher.calculate(graph)

        return buildCytoscapeReport(
            criticalPathResult,
            articulationPoints,
            matchResult,
            graph
        )
    }

    private fun buildCytoscapeReport(
        criticalPathsResult: CriticalPathsResult,
        articulationPointsResult: Set<Node>,
        matchResult: Map<Shape, Double>,
        graph: DependencyGraph,
    ): CytoscapeReportDto {
        // region Critical Path
        val criticalPaths = criticalPathsResult.expandedPaths
            .map { path ->
                path.flatMap { componentNodes -> componentNodes }
            }

        val criticalNodes = criticalPaths
            .flatten()
            .toSet()

        val criticalEdges = criticalPathsResult.expandedPaths
            .flatMap { path ->
                path.zipWithNext().flatMap { (fromComponentNodes, toComponentNodes) ->
                    graph.adjacency.flatMap { (from, targets) ->
                        targets
                            .filter { to ->
                                from in fromComponentNodes && to in toComponentNodes
                            }
                            .map { to -> from to to }
                    }
                }
            }
            .toSet()
        // endregion
        // region Modules / Groups / Nodes / Edges
        val modules = graph.adjacency
            .flatMap { (from, targets) -> listOf(from) + targets }
            .distinct()
            .sortedBy { it.id }

        val groups = modules
            .groupBy { module -> groupIdOf(module) }
            .map { (groupId, nodes) ->
                CytoscapeGroupDto(
                    id = groupId,
                    label = groupId,
                    nodes = nodes
                        .map { it.id }
                        .sorted(),
                )
            }
            .sortedBy { it.id }
        val groupElements = groups.map { group ->
            CytoscapeElementDto(
                data = CytoscapeDataDto(
                    id = group.id,
                    label = group.label,
                ),
                classes = "groupNode"
            )
        }

        val nodes = modules.map { node ->
            val groupId = groupIdOf(node)
            val isCritical = node in criticalNodes
            val isArticulationPoint = node in articulationPointsResult

            CytoscapeElementDto(
                data = CytoscapeDataDto(
                    id = node.id,
                    label = node.label,
                    group = groupId,
                    parent = groupId,
                    critical = isCritical,
                    isArticulationPoint = isArticulationPoint
                ),
                classes = if (isCritical) "critical" else "",
            )
        }

        val edges = graph.adjacency
            .flatMap { (from, targets) ->
                targets.map { to ->
                    val isCritical = from to to in criticalEdges

                    CytoscapeElementDto(
                        data = CytoscapeDataDto(
                            id = edgeId(from, to),
                            source = from.id,
                            target = to.id,
                            critical = isCritical,
                        ),
                        classes = if (isCritical) "criticalEdge" else "",
                    )
                }
            }
            .sortedWith(
                compareBy(
                    { it.data.source ?: "" },
                    { it.data.target ?: "" },
                )
            )
        // endregion
        // region Shape
        val shapeMatches = matchResult.map { (shapeMatch, similarityPercent) ->
            CytoscapeShapeSimilarityDto(
                shapeId = shapeMatch.id,
                shapeName = shapeMatch.title,
                similarityPercent = similarityPercent
            )
        }.toList()
        // endregion
        // region Plugin Settings
        val settings = MilkyWaySettings.getInstance();
        val pluginSettings = CytoscapePluginSettingsDto(
            isAnimationEnabled = settings.state.isAnimationEnabled,
            theme = settings.state.theme,
        )
        // endregion

        return CytoscapeReportDto(
            summary = CytoscapeSummaryDto(
                nodeCount = modules.size,
                edgeCount = graph.edgeCount(),
                criticalPathLength = criticalPathsResult.longestPathLength,
            ),
            elements = nodes + edges + groupElements,
            criticalPaths = criticalPaths.map { path ->
                path.map { node -> node.id }
            },
            groups = groups,
            shapeSimilarities = shapeMatches,
            cytoscapePluginSettings = pluginSettings,
        )
    }

    private fun groupIdOf(node: Node): String {
        return node.id.substringBefore(":")
    }

    private fun edgeId(from: Node, to: Node): String {
        return "${from.id}->${to.id}"
    }

}