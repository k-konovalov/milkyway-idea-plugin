package com.github.milkyway.traversers

import com.github.milkyway.DependencyTraverser
import com.github.milkyway.models.DependencyGraph
import com.github.milkyway.models.EdgeVisit
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult

class AllDependenciesTraverser : DependencyTraverser {

    override fun traverse(project: Project, graph: DependencyGraph) {
        project.configurations
            .filter { it.isCanBeResolved }
            .sortedBy { it.name }
            .forEach { configuration ->
                buildConfigurationGraph(project, configuration, graph)
            }
    }

    private fun buildConfigurationGraph(project: Project, configuration: Configuration, graph: DependencyGraph) {
        val resolutionRoot = configuration.incoming.resolutionResult.root
        val configurationNode = "${project.path}:${configuration.name}"
        val visitedEdges = mutableSetOf<EdgeVisit>()

        graph.addNode(configurationNode)

        addDependencies(graph, configurationNode, resolutionRoot.dependencies, visitedEdges)
    }

    private fun addDependencies(
        graph: DependencyGraph,
        parentNode: String,
        dependencies: Iterable<DependencyResult>,
        visitedEdges: MutableSet<EdgeVisit>
    ) {
        for (dependency in dependencies) {
            if (dependency.isConstraint) {
                continue
            }

            if (dependency is ResolvedDependencyResult) {
                val childComponent = dependency.selected
                val childId = childComponent.id

                if (childId !is ModuleComponentIdentifier) {
                    continue
                }

                val childNode = componentKey(childComponent)
                graph.addEdge(parentNode, childNode)

                val edgeVisit = EdgeVisit(parentNode, childNode)

                if (visitedEdges.add(edgeVisit)) {
                    addDependencies(graph, childNode, childComponent.dependencies, visitedEdges)
                }

            } else if (dependency is UnresolvedDependencyResult) {
                val unresolvedNode = "UNRESOLVED:${dependency.requested.displayName}"
                graph.addEdge(parentNode, unresolvedNode)
            } else {
                val requestedNode = dependency.requested.displayName
                graph.addEdge(parentNode, requestedNode)
            }
        }
    }

    private fun componentKey(component: ResolvedComponentResult): String {
        val moduleVersion = component.moduleVersion

        return if (moduleVersion == null) {
            component.id.displayName
        } else {
            "${moduleVersion.group}:${moduleVersion.name}:${moduleVersion.version}"
        }
    }
}