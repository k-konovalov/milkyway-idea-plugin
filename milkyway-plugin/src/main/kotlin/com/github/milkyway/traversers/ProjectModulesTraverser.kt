package com.github.milkyway.traversers

import com.github.milkyway.DependencyTraverser
import com.github.milkyway.models.DependencyGraph
import com.github.milkyway.models.EdgeVisit
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
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
        return configuration.dependencies.withType(org.gradle.api.artifacts.ProjectDependency::class.java).isNotEmpty()
    }

    private fun buildConfigurationGraph(graph: DependencyGraph, configuration: Configuration) {
        val resolutionRoot = configuration.incoming.resolutionResult.root
        val visitedEdges = mutableSetOf<EdgeVisit>()
        addDependencies(graph, null, resolutionRoot.dependencies, visitedEdges)
    }

    private fun addDependencies(
        graph: DependencyGraph,
        parentNode: String?,
        dependencies: Iterable<DependencyResult>,
        visitedEdges: MutableSet<EdgeVisit>
    ) {
        for (dependency in dependencies) {
            if (dependency.isConstraint || dependency !is ResolvedDependencyResult) {
                continue
            }

            val childComponent = dependency.selected
            val childNode = projectComponentKeyOrNull(childComponent)

            if (attachNode(childNode, parentNode, graph, visitedEdges)) {
                addDependencies(graph, childNode, childComponent.dependencies, visitedEdges)
            }
        }
    }

    private fun attachNode(
        childNode: String?,
        parentNode: String?,
        graph: DependencyGraph,
        visitedEdges: MutableSet<EdgeVisit>
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

    private fun projectComponentKeyOrNull(component: ResolvedComponentResult): String? {
        val componentId = component.id as? ProjectComponentIdentifier ?: return null
        if (isRootProject(componentId)) {
            return null
        }
        return componentId.projectPath.removePrefix(":")
    }

    private fun isRootProject(componentId: ProjectComponentIdentifier): Boolean {
        return componentId.projectPath == ":"
    }

}