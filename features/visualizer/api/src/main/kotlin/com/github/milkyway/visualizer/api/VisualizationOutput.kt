package com.github.milkyway.visualizer.api

import java.nio.file.Path

sealed class VisualizationOutput {
    sealed class Browser : VisualizationOutput() {
        data class Html(val content: String) : Browser()
    }
    sealed class Static : VisualizationOutput() {
        data class ImageFile(val path: Path) : Static()
    }
}
