package com.github.milkyway.idea

import com.github.milkyway.algorithm.articulationpoints.ArticulationPointsAnalyzer
import com.github.milkyway.algorithm.criticalpath.CriticalPathAnalyzer
import com.github.milkyway.algorithm.shapematching.GraphShapeMatcher
import com.github.milkyway.visualizer.cytoscape.CytoscapePluginSettingsDto
import com.github.milkyway.core.resolver.DependencyResolver
import com.github.milkyway.idea.feature.GraphCutter
import com.github.milkyway.idea.pipeline.AnalysisPipeline
import com.github.milkyway.idea.resolver.GradleDependencyResolver
import com.github.milkyway.idea.resolver.RegexDependencyResolver
import com.github.milkyway.idea.settings.MilkyWaySettings
import com.github.milkyway.visualizer.api.VisualizationOutput
import com.github.milkyway.visualizer.cytoscape.CytoscapeVisualizer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.runBlocking
import java.io.File

class GradleDependencyAnalysisRunner(
    private val ideaProject: Project,
    private val settings: MilkyWaySettings = MilkyWaySettings.getInstance(),
    private val srcGradleFile: VirtualFile? = null,
) {
    fun run(projectDir: File): String {
        println("Gradle Traverse started")
        val isGradleParser = settings.state.parser == MilkyWaySettings.PARSER_GRADLE
        println("settings.state ${settings.state}")
        val resolver: DependencyResolver = if (isGradleParser) {
            println("Parsing by gradle")
            GradleDependencyResolver(ideaProject, projectDir)
        } else {
            println("Parsing by regex")
            RegexDependencyResolver(ideaProject)
        }

        val graph = resolver.resolve()
        graph.adjacency.forEach { (module, children) ->
            println("${module}: [${children}]")
        }

        val shrunkGraph = GraphCutter(ideaProject, graph, srcGradleFile).cut()

        val pipeline = AnalysisPipeline(
            listOf(
                CriticalPathAnalyzer(),
                ArticulationPointsAnalyzer(),
                GraphShapeMatcher(),
            )
        )

        val analysisResult = runBlocking { pipeline.run(shrunkGraph) }

        val pluginSettings = CytoscapePluginSettingsDto(
            isAnimationEnabled = settings.state.isAnimationEnabled,
            theme = settings.state.theme,
            isWebGlEnabled = settings.state.isWebGlEnabled,
            isGroupOnLoadEnabled = settings.state.isGroupOnLoadEnabled,
        )

        val output = CytoscapeVisualizer(pluginSettings).render(analysisResult)
        return (output as VisualizationOutput.Browser.Html).content
    }
}
