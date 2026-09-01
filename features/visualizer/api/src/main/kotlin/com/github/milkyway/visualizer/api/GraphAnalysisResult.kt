package com.github.milkyway.visualizer.api

import com.github.milkyway.algorithm.api.AnalyzerResult
import com.github.milkyway.core.models.DependencyGraph
import kotlin.reflect.KClass

class GraphAnalysisResult(
    val graph: DependencyGraph,
    private val results: Map<KClass<out AnalyzerResult>, AnalyzerResult>,
) {
    fun <R : AnalyzerResult> get(klass: KClass<R>): AnalyzerResult =
        results[klass] ?: AnalyzerResult.Empty<R>()
}
