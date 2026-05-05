package com.github.milkyway.idea.cytoscape

import com.github.milkyway.core.analyzer.CriticalPathAnalyzer
import com.github.milkyway.core.models.CriticalPathsResult
import com.github.milkyway.core.models.CytoscapeDataDto
import com.github.milkyway.core.models.CytoscapeElementDto
import com.github.milkyway.core.models.CytoscapeGroupDto
import com.github.milkyway.core.models.CytoscapeReportDto
import com.github.milkyway.core.models.CytoscapeSummaryDto
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node

class ReportBuilder {

    fun build(graph: DependencyGraph): CytoscapeReportDto {
        val analyzer = CriticalPathAnalyzer()
        val result = analyzer.findCriticalPaths(graph)

        return buildCytoscapeReport(result, graph)
    }

    private fun buildCytoscapeReport(
        result: CriticalPathsResult,
        graph: DependencyGraph,
    ): CytoscapeReportDto {
        val criticalPaths = result.expandedPaths
            .map { path ->
                path.flatMap { componentNodes -> componentNodes }
            }

        val criticalNodes = criticalPaths
            .flatten()
            .toSet()

        val criticalEdges = result.expandedPaths
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

        val nodes = modules.map { node ->
            val groupId = groupIdOf(node)
            val isCritical = node in criticalNodes

            CytoscapeElementDto(
                data = CytoscapeDataDto(
                    id = node.id,
                    label = node.label,
                    group = groupId,
                    critical = isCritical,
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

        return CytoscapeReportDto(
            summary = CytoscapeSummaryDto(
                nodeCount = modules.size,
                edgeCount = graph.edgeCount(),
                criticalPathLength = result.longestPathLength,
            ),
            elements = nodes + edges,
            criticalPaths = criticalPaths.map { path ->
                path.map { node -> node.id }
            },
            groups = groups,
        )
    }

    private fun groupIdOf(node: Node): String {
        return node.id.substringBefore(":")
    }

    private fun edgeId(from: Node, to: Node): String {
        return "${from.id}->${to.id}"
    }

}