package com.github.milkyway.gradle.tasks

import com.github.milkyway.core.MilkyWayConstants
import com.github.milkyway.core.analyzer.CriticalPathAnalyzer
import com.github.milkyway.core.models.CriticalPathsResult
import com.github.milkyway.core.models.CytoscapeDataDto
import com.github.milkyway.core.models.CytoscapeElementDto
import com.github.milkyway.core.models.CytoscapeGroupDto
import com.github.milkyway.core.models.CytoscapeReportDto
import com.github.milkyway.core.models.CytoscapeSummaryDto
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.gradle.traversers.ProjectModulesTraverser
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class PrintDependenciesTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun printAll() {
        val dir = outputDir.get().asFile

        cleanOutputDir(dir)

        val graph = DependencyGraph()
        val traverser = ProjectModulesTraverser()

        for (subproject in project.rootProject.allprojects) {
            traverser.traverse(subproject, graph)
        }

        val analyzer = CriticalPathAnalyzer()
        val result = analyzer.findCriticalPaths(graph)

        val cytoscapeReport = buildCytoscapeReport(result, graph)

        val cytoscapeFile = dir.resolve(MilkyWayConstants.CYTOSCAPE_REPORT_FILE)
        val text = Json {
            prettyPrint = true
            encodeDefaults = true
        }.encodeToString(cytoscapeReport)

        cytoscapeFile.writeText(text)
    }

    private fun cleanOutputDir(dir: File) {
        if (dir.exists() && !dir.isDirectory) {
            dir.delete()
        }
        dir.mkdirs()
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

        val moduleIds = graph.adjacency
            .flatMap { (from, targets) -> listOf(from) + targets }
            .distinct()
            .sorted()

        val groups = moduleIds
            .groupBy { moduleId -> moduleId.substringBefore(":") }
            .map { (groupId, nodes) ->
                CytoscapeGroupDto(
                    id = groupId,
                    label = groupId,
                    nodes = nodes.sorted(),
                )
            }
            .sortedBy { it.id }

        val nodes = moduleIds.map { node ->
            val groupId = node.substringBefore(":")

            CytoscapeElementDto(
                data = CytoscapeDataDto(
                    id = node,
                    label = node,
                    group = groupId,
                    critical = node in criticalNodes,
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
                            id = "$from->$to",
                            source = from,
                            target = to,
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
                nodeCount = moduleIds.size,
                edgeCount = graph.edgeCount(),
                criticalPathLength = result.longestPathLength,
            ),
            elements = nodes + edges,
            criticalPaths = criticalPaths,
            groups = groups,
        )
    }

}