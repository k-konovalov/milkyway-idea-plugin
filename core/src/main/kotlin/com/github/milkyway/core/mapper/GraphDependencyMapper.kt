package com.github.milkyway.core.mapper

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.DependencyGraphDto
import com.github.milkyway.core.models.EdgeDto
import com.github.milkyway.core.models.Node
import com.github.milkyway.core.models.NodeDto

object GraphDependencyMapper {

    fun toDto(graph: DependencyGraph): DependencyGraphDto {
        val nodes = graph.adjacency
            .flatMap { (from, targets) -> listOf(from) + targets }
            .distinct()
            .sortedBy { it.id }
            .map { node ->
                NodeDto(
                    id = node.id,
                    label = node.label,
                )
            }

        val edges = graph.adjacency
            .flatMap { (from, targets) ->
                targets.map { to ->
                    EdgeDto(
                        from = from.id,
                        to = to.id,
                    )
                }
            }
            .sortedWith(
                compareBy(
                    { it.from },
                    { it.to },
                )
            )

        return DependencyGraphDto(
            nodes = nodes,
            edges = edges,
        )
    }

    fun fromDto(dto: DependencyGraphDto): DependencyGraph {
        val graph = DependencyGraph()

        val nodesById = dto.nodes.associate { node ->
            node.id to Node(
                id = node.id,
                label = node.label,
            )
        }

        nodesById.values.forEach { node ->
            graph.addNode(node)
        }

        dto.edges.forEach { edge ->
            val from = nodesById.getValue(edge.from)
            val to = nodesById.getValue(edge.to)

            graph.addEdge(from, to)
        }

        return graph
    }

}