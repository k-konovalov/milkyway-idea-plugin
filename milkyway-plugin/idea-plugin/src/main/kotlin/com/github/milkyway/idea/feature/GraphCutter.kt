package com.github.milkyway.idea.feature

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.Node
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

class GraphCutter(
    private val project: Project,
    private val dependencyGraph: DependencyGraph,
    private val srcGradleFile: VirtualFile? = null,
) {
    fun cut(): DependencyGraph {
        if (srcGradleFile == null) {
            return dependencyGraph
        }
        val moduleName = moduleNameFromFile(project, srcGradleFile).removePrefix(":")
        val shrunkGraph = dependencyGraph.startFromModuleName(moduleName)
        return shrunkGraph
    }

    private fun moduleNameFromFile(project: Project, file: VirtualFile): String {
        val relativePath = VfsUtilCore.getRelativePath(file.parent, project.baseDir) ?: return ""
        return ":" + relativePath.replace("/", ":")
    }

    private fun DependencyGraph.startFromModuleName(moduleName: String): DependencyGraph {
        val srcNode = Node(moduleName)
        if (srcNode !in adjacency.keys) {
            return this
        }

        val queue = ArrayDeque<Node>()
        queue.add(srcNode)

        val shrunkGraph = DependencyGraph()
        val visited = mutableSetOf<Node>()

        while (queue.isNotEmpty()) {
            val from = queue.removeFirst()
            if (from in visited) {
                continue
            }
            visited.add(from)
            for (to in adjacency.getValue(from)) {
                shrunkGraph.addEdge(from, to)
                queue.add(to)
            }
        }
        return shrunkGraph
    }
}
