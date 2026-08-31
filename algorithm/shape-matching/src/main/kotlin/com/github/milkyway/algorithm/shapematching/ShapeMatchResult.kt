package com.github.milkyway.algorithm.shapematching

import com.github.milkyway.algorithm.api.AnalyzerResult

data class ShapeMatchResult(
    val similarities: Map<Shape, Double>,
) : AnalyzerResult
