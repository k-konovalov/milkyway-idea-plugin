package com.github.milkyway.algorithm.shapematching.api

import com.github.milkyway.algorithm.api.AnalyzerResult

data class ShapeMatchResult(
    val similarities: Map<Shape, Double>,
) : AnalyzerResult
