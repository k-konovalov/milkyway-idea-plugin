package com.github.milkyway.graph

import com.github.milkyway.algorithm.api.AnalyzerResult
import com.github.milkyway.algorithm.api.GraphAnalyzer
import com.github.milkyway.algorithm.articulationpoints.ArticulationPointsAnalyzer
import com.github.milkyway.algorithm.criticalpath.CriticalPathAnalyzer
import com.github.milkyway.algorithm.shapematching.GraphShapeMatcher
import com.github.milkyway.parser.api.DependencyResolver
import com.github.milkyway.visualizer.api.VisualizationOutput
import com.github.milkyway.visualizer.cytoscape.CytoscapePluginSettingsDto
import com.github.milkyway.visualizer.cytoscape.CytoscapeVisualizer
import kotlinx.coroutines.runBlocking

class GraphAnalysisRunner(
    private val resolver: DependencyResolver,
    private val moduleName: String? = null,
    private val cytoscapeSettings: CytoscapePluginSettingsDto,
) {
    fun run(): String {
        val graph = resolver.resolve()
        val shrunkGraph = GraphCutter(graph, moduleName).cut()
        val analyzers = listOf<GraphAnalyzer<AnalyzerResult>>(
            CriticalPathAnalyzer(),
            ArticulationPointsAnalyzer(),
            GraphShapeMatcher(),
        )
        val result = runBlocking { AnalysisPipeline(analyzers).run(shrunkGraph) }
        val output = CytoscapeVisualizer(cytoscapeSettings).render(result)
        return (output as VisualizationOutput.Browser.Html).content
    }
}
