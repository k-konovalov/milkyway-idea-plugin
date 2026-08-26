package com.github.milkyway.core.analyzer

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node
import com.github.milkyway.core.models.StronglyConnectedComponent

class TarjanSccFinder(
    private val graph: DependencyGraph,
) {
    private var index = 0

    private val indexMap = mutableMapOf<Node, Int>()
    private val lowLinkMap = mutableMapOf<Node, Int>()
    private val stack = ArrayDeque<Node>()
    private val onStack = mutableSetOf<Node>()
    private val result = mutableListOf<StronglyConnectedComponent>()

    fun find(): List<StronglyConnectedComponent> {
        graph.adjacency.keys
            .sortedBy { it.id }
            .filter { it !in indexMap }
            .forEach { strongConnect(it) }

        return result
    }

    private fun strongConnect(node: Node) {
        indexMap[node] = index
        lowLinkMap[node] = index
        index++

        stack.addLast(node)
        onStack.add(node)

        graph.adjacency[node].orEmpty().forEach { neighbor ->
            when (neighbor) {
                !in indexMap -> {
                    strongConnect(neighbor)
                    lowLinkMap[node] = minOf(
                        lowLinkMap.getValue(node),
                        lowLinkMap.getValue(neighbor),
                    )
                }

                in onStack -> {
                    lowLinkMap[node] = minOf(
                        lowLinkMap.getValue(node),
                        indexMap.getValue(neighbor),
                    )
                }
            }
        }

        if (lowLinkMap.getValue(node) == indexMap.getValue(node)) {
            result += popComponent(node)
        }
    }

    private fun popComponent(root: Node): StronglyConnectedComponent {
        val componentNodes = linkedSetOf<Node>()

        while (true) {
            val top = stack.removeLast()
            onStack.remove(top)
            componentNodes.add(top)

            if (top == root) {
                break
            }
        }

        return StronglyConnectedComponent(
            id = result.size,
            nodes = componentNodes,
        )
    }

}