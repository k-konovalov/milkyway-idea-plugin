package com.github.milkyway.algorithm.criticalpath.api

import com.github.milkyway.algorithm.api.AnalyzerResult
import com.github.milkyway.core.models.Node

data class CriticalPathResult(
    val longestPathLength: Int,
    val expandedPaths: List<List<Set<Node>>>,
) : AnalyzerResult
