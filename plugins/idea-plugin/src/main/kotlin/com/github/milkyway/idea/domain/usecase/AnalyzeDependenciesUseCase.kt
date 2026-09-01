package com.github.milkyway.idea.domain.usecase

import com.github.milkyway.graph.GraphAnalysisRunner
import com.github.milkyway.parser.api.DependencyResolver
import com.github.milkyway.visualizer.cytoscape.CytoscapePluginSettingsDto

class AnalyzeDependenciesUseCase(
    private val resolver: DependencyResolver,
    private val moduleName: String?,
    private val cytoscapeSettings: CytoscapePluginSettingsDto,
) {
    fun execute(): String = GraphAnalysisRunner(resolver, moduleName, cytoscapeSettings).run()
}
