package com.github.milkyway.visualizer.api

interface Visualizer {
    fun render(result: GraphAnalysisResult): VisualizationOutput
}
