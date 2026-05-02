package com.github.milkyway.plugin.model

class ArticulationPointsGraph(edges: List<Pair<String, String>>) {
    private val nodes: MutableMap<String, Node> = mutableMapOf()
    private val directedEdges: MutableList<Edge> = mutableListOf()

    init {
        // region  Add Edges and Nodes
        for ((fromName, toName) in edges) {
            val from = nodes.getOrPut(fromName) { Node(fromName) }
            val to = nodes.getOrPut(toName) { Node(toName) }
            from.neighbours.add(to)
            to.neighbours.add(from)
            directedEdges.add(Edge(from, to))
        }
        // endregion

        // region Remove Duplicates
        for (node in nodes.values) {
            node.neighbours
                .distinctBy { it.name }
                .sortedBy { it.name }
                .let {
                    // At this point we have distinct children. Replace current children by them.
                    node.neighbours.clear()
                    node.neighbours.addAll(it)
                }
        }
        // endregion
    }

    /**
     * Articulation point --- point, which create 2 subgraph if removed.
     */
    fun findArticulationPoints(): List<Node> {
        val tin = mutableMapOf<Node, Int>()
        val low = mutableMapOf<Node, Int>()
        val visited = mutableSetOf<Node>()
        val articulation = mutableSetOf<Node>()
        var timer = 0

        for (start in nodes.values) {
            if (start in visited) continue

            // from, parent, nextNeighbourIndex
            val stack = ArrayDeque<Triple<Node, Node?, Int>>()
            val childrenCount = mutableMapOf<Node, Int>().withDefault { 0 }
            stack.addLast(Triple(start, null, 0))

            while (stack.isNotEmpty()) {
                val (from, parent, nextNeighbourIndex) = stack.removeLast()

                if (from !in visited) {
                    visited.add(from)
                    tin[from] = timer
                    low[from] = timer
                    ++timer
                }

                // region Traverse Neighbour
                if (nextNeighbourIndex < from.neighbours.size) {
                    val to = from.neighbours[nextNeighbourIndex]
                    stack.addLast(Triple(from, parent, nextNeighbourIndex + 1))

                    if (to == parent) {
                        continue
                    }
                    if (to in visited) {
                        low[from] = minOf(low[from]!!, tin[to]!!)
                    } else {
                        childrenCount[from] = childrenCount.getValue(from) + 1
                        stack.addLast(Triple(to, from, 0))
                    }
                    continue
                }
                // endregion
                // region Else Exit from Vertex
                if (parent != null) {
                    low[parent] = minOf(low[parent]!!, low[from]!!)

                    if (low[from]!! >= tin[parent]!!) {
                        articulation.add(parent)
                    }
                } else {
                    /**
                     * it's root
                     * If it has more than 1 child, it might be critical vertex
                     */
                    if (childrenCount.getValue(from) > 1) {
                        articulation.add(from)
                    }
                }
                // endregion
            }

        }

        return articulation.sortedBy { it.name }
    }

    fun getDirectedEdges(): List<Edge> = directedEdges
}