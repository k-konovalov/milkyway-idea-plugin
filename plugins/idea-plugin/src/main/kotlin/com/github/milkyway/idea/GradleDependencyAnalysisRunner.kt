package com.github.milkyway.idea

import com.github.milkyway.graph.GraphAnalysisRunner
import com.github.milkyway.idea.resolver.GradleDependencyResolver
import com.github.milkyway.idea.resolver.RegexDependencyResolver
import com.github.milkyway.idea.settings.MilkyWaySettings
import com.github.milkyway.visualizer.cytoscape.CytoscapePluginSettingsDto
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class GradleDependencyAnalysisRunner(
    private val ideaProject: Project,
    private val settings: MilkyWaySettings = MilkyWaySettings.getInstance(),
    private val srcGradleFile: VirtualFile? = null,
) {
    fun run(projectDir: File): String {
        val resolver = if (settings.state.parser == MilkyWaySettings.PARSER_GRADLE) {
            GradleDependencyResolver(ideaProject, projectDir)
        } else {
            RegexDependencyResolver(ideaProject)
        }

        val moduleName = srcGradleFile?.let { file ->
            val relativePath = VfsUtilCore.getRelativePath(file.parent, ideaProject.baseDir)
                ?: return@let null
            (":$relativePath").replace("/", ":").removePrefix(":")
        }

        val cytoscapeSettings = CytoscapePluginSettingsDto(
            isAnimationEnabled = settings.state.isAnimationEnabled,
            theme = settings.state.theme,
            isWebGlEnabled = settings.state.isWebGlEnabled,
            isGroupOnLoadEnabled = settings.state.isGroupOnLoadEnabled,
        )

        return GraphAnalysisRunner(resolver, moduleName, cytoscapeSettings).run()
    }
}
