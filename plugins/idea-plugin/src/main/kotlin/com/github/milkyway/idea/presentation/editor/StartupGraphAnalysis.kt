package com.github.milkyway.idea.presentation.editor

import com.github.milkyway.idea.data.source.IjDependencyAnalysisAdapter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class StartupGraphAnalysis(
    private val project: Project,
    private val file: VirtualFile,
    private val previewEditor: MilkywayPreviewEditor,
) {
    fun run() {
        ProgressManager.getInstance().run(
            object: Task.Backgroundable(
                project, "Analyzing Gradle dependencies", true
            ) {
                override fun run(indicator: ProgressIndicator) {
                    val basePath = project.basePath ?: return
                    val projectDir = File(basePath)
                    val rootFile = if (file.name == "settings.gradle.kts") { null } else { file }
                    try {
                        indicator.text = "Running Gradle analysis"
                        val cyJson = IjDependencyAnalysisAdapter(project, srcGradleFile = rootFile).run(projectDir)
                        previewEditor.reload(cyJson)
                    } catch (e: Exception) {
                        ApplicationManager.getApplication().invokeLater {
                            showErrorDialog(
                                project,
                                e.message ?: "Unknown error",
                                "Milkyway Dependency Analysis Failed"
                            )
                        }
                    }
                }
            }
        )
    }
}
