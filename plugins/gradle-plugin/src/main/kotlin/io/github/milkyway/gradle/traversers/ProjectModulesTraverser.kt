package io.github.milkyway.gradle.traversers

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.core.models.EdgeVisit
import com.github.milkyway.core.models.Node
import io.github.milkyway.gradle.DependencyTraverser
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class ProjectModulesTraverser : DependencyTraverser {

    override fun traverse(project: Project, graph: DependencyGraph) {
        project.configurations
            .filter { it.isCanBeResolved && isMainProjectGraphConfiguration(it) }
            .forEach { configuration ->
                buildConfigurationGraph(graph, configuration)
            }
    }

    private fun isMainProjectGraphConfiguration(configuration: Configuration): Boolean {
        return configuration.dependencies.withType(ProjectDependency::class.java).isNotEmpty()
    }

    private fun buildConfigurationGraph(graph: DependencyGraph, configuration: Configuration) {
        val resolutionRoot = configuration.incoming.resolutionResult.root
        val visitedEdges = mutableSetOf<EdgeVisit>()

        addDependencies(
            graph = graph,
            parentNode = null,
            dependencies = resolutionRoot.dependencies,
            visitedEdges = visitedEdges,
        )
    }

    private fun addDependencies(
        graph: DependencyGraph,
        parentNode: Node?,
        dependencies: Iterable<DependencyResult>,
        visitedEdges: MutableSet<EdgeVisit>,
    ) {
        for (dependency in dependencies) {
            if (dependency.isConstraint || dependency !is ResolvedDependencyResult) {
                continue
            }

            val childComponent = dependency.selected
            val childNode = projectComponentNodeOrNull(childComponent)

            if (attachNode(childNode, parentNode, graph, visitedEdges)) {
                addDependencies(
                    graph = graph,
                    parentNode = childNode,
                    dependencies = childComponent.dependencies,
                    visitedEdges = visitedEdges,
                )
            }
        }
    }

    private fun attachNode(
        childNode: Node?,
        parentNode: Node?,
        graph: DependencyGraph,
        visitedEdges: MutableSet<EdgeVisit>,
    ): Boolean {
        if (childNode == null) {
            return false
        }

        if (parentNode == null) {
            graph.addNode(childNode)
            return true
        }

        if (childNode == parentNode) {
            return false
        }

        val edgeVisit = EdgeVisit(parentNode, childNode)

        if (!visitedEdges.add(edgeVisit)) {
            return false
        }

        graph.addEdge(parentNode, childNode)
        return true
    }

    private fun projectComponentNodeOrNull(component: ResolvedComponentResult): Node? {
        val componentId = component.id as? ProjectComponentIdentifier ?: return null

        if (isRootProject(componentId)) {
            return null
        }

        return Node(componentId.projectPath.removePrefix(":"))
    }

    private fun isRootProject(componentId: ProjectComponentIdentifier): Boolean {
        return componentId.projectPath == ":"
    }

}