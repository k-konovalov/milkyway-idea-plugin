package com.github.milkyway.idea

import com.github.milkyway.idea.toolwindow.MilkyWayToolWindowOpener
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages.showErrorDialog
import java.io.File

class AnalyzeDependenciesAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(event: AnActionEvent) {
        val project = event.project
        val basePath = project?.basePath

        event.presentation.isVisible = true
        event.presentation.isEnabled = basePath != null && isGradleProject(File(basePath))
    }

    override fun actionPerformed(event: AnActionEvent) {
        val ideaProject = event.project ?: return
        val basePath = ideaProject.basePath ?: return
        val projectDir = File(basePath)

        if (!isGradleProject(projectDir)) {
            showErrorDialog(
                ideaProject,
                "Opened project is not a Gradle project.",
                "MilkyWay Dependency Analysis"
            )
            return
        }

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(
                ideaProject,
                "Analyzing Gradle Dependencies",
                true
            ) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        indicator.text = "Running Gradle analysis"

                        val cytoscapeJson = GradleDependencyAnalysisRunner(ideaProject).run(projectDir)

                        ApplicationManager.getApplication().invokeLater {
                            ideaProject.milkyWayReportService().update(cytoscapeJson)
                            MilkyWayToolWindowOpener.open(ideaProject)
                        }
                    } catch (exception: Exception) {
                        ApplicationManager.getApplication().invokeLater {
                            showErrorDialog(
                                ideaProject,
                                exception.message ?: "Unknown error",
                                "MilkyWay Dependency Analysis Failed"
                            )
                        }
                    }
                }
            }
        )
    }

    private fun isGradleProject(projectDir: File): Boolean {
        return File(projectDir, "settings.gradle.kts").exists() ||
                File(projectDir, "settings.gradle").exists() ||
                File(projectDir, "build.gradle.kts").exists() ||
                File(projectDir, "build.gradle").exists()
    }

}