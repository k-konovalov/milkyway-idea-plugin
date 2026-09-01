package com.github.milkyway.graph

import com.github.milkyway.algorithm.api.AnalyzerResult
import com.github.milkyway.algorithm.api.GraphAnalyzer
import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.visualizer.api.GraphAnalysisResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

class AnalysisPipeline(
    private val analyzers: List<GraphAnalyzer<out AnalyzerResult>>,
) {
    suspend fun run(graph: DependencyGraph): GraphAnalysisResult = coroutineScope {
        val results: Map<KClass<out AnalyzerResult>, AnalyzerResult> = analyzers
            .map { async { it.analyze(graph) } }
            .awaitAll()
            .associateBy { it::class }
        GraphAnalysisResult(graph, results)
    }
}
