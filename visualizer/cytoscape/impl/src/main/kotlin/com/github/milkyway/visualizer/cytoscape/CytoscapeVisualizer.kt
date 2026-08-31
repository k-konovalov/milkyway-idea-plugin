package com.github.milkyway.visualizer.cytoscape

import com.github.milkyway.algorithm.articulationpoints.ArticulationPointsResult
import com.github.milkyway.algorithm.criticalpath.CriticalPathResult
import com.github.milkyway.algorithm.shapematching.Shape
import com.github.milkyway.algorithm.shapematching.ShapeMatchResult
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node
import com.github.milkyway.visualizer.api.GraphAnalysisResult
import com.github.milkyway.visualizer.api.VisualizationOutput
import com.github.milkyway.visualizer.api.Visualizer
import kotlinx.serialization.json.Json

class CytoscapeVisualizer(
    private val pluginSettings: CytoscapePluginSettingsDto,
) : Visualizer {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override fun render(result: GraphAnalysisResult): VisualizationOutput.Browser.Html {
        val report = buildReport(result)
        val reportJson = json.encodeToString(report)
        return VisualizationOutput.Browser.Html(renderHtml(reportJson))
    }

    private fun buildReport(result: GraphAnalysisResult): CytoscapeReportDto {
        val criticalPathResult = result.get(CriticalPathResult::class)
        val articulationResult = result.get(ArticulationPointsResult::class)
        val shapeResult = result.get(ShapeMatchResult::class)

        val criticalPaths: List<List<Set<Node>>>
        val longestPathLength: Int
        if (criticalPathResult is CriticalPathResult) {
            criticalPaths = criticalPathResult.expandedPaths
            longestPathLength = criticalPathResult.longestPathLength
        } else {
            criticalPaths = emptyList()
            longestPathLength = 0
        }

        val articulationPoints: Set<Node> = if (articulationResult is ArticulationPointsResult) {
            articulationResult.points
        } else {
            emptySet()
        }

        val shapeMatches: Map<Shape, Double> = if (shapeResult is ShapeMatchResult) {
            shapeResult.similarities
        } else {
            emptyMap()
        }

        return buildCytoscapeReport(
            criticalPaths = criticalPaths,
            longestPathLength = longestPathLength,
            articulationPoints = articulationPoints,
            shapeMatches = shapeMatches,
            graph = result.graph,
        )
    }

    private fun buildCytoscapeReport(
        criticalPaths: List<List<Set<Node>>>,
        longestPathLength: Int,
        articulationPoints: Set<Node>,
        shapeMatches: Map<Shape, Double>,
        graph: DependencyGraph,
    ): CytoscapeReportDto {
        val expandedCriticalPaths = criticalPaths.map { path ->
            path.flatMap { componentNodes -> componentNodes }
        }
        val criticalNodes = expandedCriticalPaths.flatten().toSet()
        val criticalEdges = criticalPaths
            .flatMap { path ->
                path.zipWithNext().flatMap { (fromComponentNodes, toComponentNodes) ->
                    graph.adjacency.flatMap { (from, targets) ->
                        targets
                            .filter { to -> from in fromComponentNodes && to in toComponentNodes }
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
            .groupBy { groupIdOf(it) }
            .map { (groupId, nodes) ->
                CytoscapeGroupDto(
                    id = groupId,
                    label = groupId,
                    nodes = nodes.map { it.id }.sorted(),
                )
            }
            .sortedBy { it.id }

        val groupElements = groups.map { group ->
            CytoscapeElementDto(
                data = CytoscapeDataDto(id = group.id, label = group.label),
                classes = "groupNode",
            )
        }

        val nodes = modules.map { node ->
            val groupId = groupIdOf(node)
            CytoscapeElementDto(
                data = CytoscapeDataDto(
                    id = node.id,
                    label = node.label,
                    group = groupId,
                    parent = groupId,
                    critical = node in criticalNodes,
                    isArticulationPoint = node in articulationPoints,
                ),
                classes = if (node in criticalNodes) "critical" else "",
            )
        }

        val edges = graph.adjacency
            .flatMap { (from, targets) ->
                targets.map { to ->
                    val isCritical = from to to in criticalEdges
                    CytoscapeElementDto(
                        data = CytoscapeDataDto(
                            id = "${from.id}->${to.id}",
                            source = from.id,
                            target = to.id,
                            critical = isCritical,
                        ),
                        classes = if (isCritical) "criticalEdge" else "",
                    )
                }
            }
            .sortedWith(compareBy({ it.data.source ?: "" }, { it.data.target ?: "" }))

        val shapeSimilarities = shapeMatches.map { (shape, percent) ->
            CytoscapeShapeSimilarityDto(
                shapeId = shape.id,
                shapeName = shape.title,
                similarityPercent = percent,
            )
        }

        return CytoscapeReportDto(
            summary = CytoscapeSummaryDto(
                nodeCount = modules.size,
                edgeCount = graph.edgeCount(),
                criticalPathLength = longestPathLength,
            ),
            elements = nodes + edges + groupElements,
            criticalPaths = expandedCriticalPaths.map { path -> path.map { it.id } },
            groups = groups,
            shapeSimilarities = shapeSimilarities,
            cytoscapePluginSettings = pluginSettings,
        )
    }

    private fun groupIdOf(node: Node): String = node.id.substringBefore(":")

    private fun renderHtml(cytoscapeJson: String): String {
        val html = loadResource("/web/cytoscape.html")
        val css = loadResource("/web/cytoscape.css")
        val cytoscapeJs = loadResource("/web/cytoscape.min.js")
        val cytoscapeExpandCollapseJs = loadResource("/web/cytoscape-expand-collapse.min.js")
        val cytoscapeUndoRedo = loadResource("/web/cytoscape-undo-redo.js")
        val klay = loadResource("/web/klay.js")
        val cyKlay = loadResource("/web/cytoscape-klay.js")
        val viewJs = loadResource("/web/cytoscape-view.js")

        val safeJson = cytoscapeJson.replace("</script>", "<\\/script>")

        return html
            .replace("{{MILKYWAY_CSS}}", css)
            .replace("{{CYTOSCAPE_JS}}", cytoscapeJs)
            .replace("{{MILKYWAY_REPORT_JSON}}", safeJson)
            .replace("{{MILKYWAY_VIEW_JS}}", viewJs)
            .replace("{{CYTOSCAPE_EXPAND_COLLAPSE_JS}}", cytoscapeExpandCollapseJs)
            .replace("{{CYTOSCAPE_UNDO_REDO_JS}}", cytoscapeUndoRedo)
            .replace("{{KLAY}}", klay)
            .replace("{{CYTOSCAPE_KLAY}}", cyKlay)
    }

    private fun loadResource(path: String): String {
        return CytoscapeVisualizer::class.java
            .getResourceAsStream(path)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Resource not found: $path")
    }
}

