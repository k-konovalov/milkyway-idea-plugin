package com.github.milkyway.algorithm.articulationpoints

import com.github.milkyway.algorithm.api.AnalyzerResult
import com.github.milkyway.core.models.Node

data class ArticulationPointsResult(
    val points: Set<Node>,
) : AnalyzerResult
