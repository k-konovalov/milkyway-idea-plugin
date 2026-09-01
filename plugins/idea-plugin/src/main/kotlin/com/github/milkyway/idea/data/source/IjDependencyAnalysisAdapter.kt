package com.github.milkyway.idea.data.source

import com.github.milkyway.idea.domain.usecase.AnalyzeDependenciesUseCase
import com.github.milkyway.idea.platform.settings.ParserSettings
import com.github.milkyway.idea.platform.settings.VisualizerSettings
import com.github.milkyway.visualizer.cytoscape.CytoscapePluginSettingsDto
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class IjDependencyAnalysisAdapter(
    private val ideaProject: Project,
    private val srcGradleFile: VirtualFile? = null,
    private val parserSettings: ParserSettings = ParserSettings.getInstance(),
    private val vizSettings: VisualizerSettings = VisualizerSettings.getInstance(),
) {
    fun run(projectDir: File): String {
        val resolver = if (parserSettings.state.parser == ParserSettings.PARSER_GRADLE) {
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
            isAnimationEnabled = vizSettings.state.isAnimationEnabled,
            theme = vizSettings.state.theme,
            isWebGlEnabled = vizSettings.state.isWebGlEnabled,
            isGroupOnLoadEnabled = vizSettings.state.isGroupOnLoadEnabled,
        )

        return AnalyzeDependenciesUseCase(resolver, moduleName, cytoscapeSettings).execute()
    }
}
