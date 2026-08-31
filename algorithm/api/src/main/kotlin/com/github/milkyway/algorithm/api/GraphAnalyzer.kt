package com.github.milkyway.algorithm.api

import com.github.milkyway.core.models.DependencyGraph

interface GraphAnalyzer<out R : AnalyzerResult> {
    suspend fun analyze(graph: DependencyGraph): R
}
