package com.github.milkyway.idea

import com.github.milkyway.idea.settings.MilkyWaySettings
import com.github.milkyway.idea.toolwindow.MilkyWayToolWindowOpener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class GradleFileListener(
    project: Project
) {
    init {
        val settings = MilkyWaySettings.getInstance()
        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    if (!settings.state.isRerenderOnFileOpenEnabled) {
                        return
                    }
                    val filename = file.name
                    val filePath = file.path
                    if (filename != "settings.gradle.kts" && filename != "build.gradle.kts") {
                        return
                    }
                    println("Opening $filename from $filePath")

                    val basePath = project.basePath ?: return
                    val projectDir = File(basePath)

                    ProgressManager.getInstance().run(
                        object : Task.Backgroundable(
                            project,
                            "Analyzing Gradle Dependencies",
                            true
                        ) {
                            override fun run(indicator: ProgressIndicator) {
                                try {
                                    indicator.text = "Running Gradle analysis"

                                    val cytoscapeJson = GradleDependencyAnalysisRunner(project, srcGradleFile = file).run(projectDir)

                                    ApplicationManager.getApplication().invokeLater {
                                        project.milkyWayReportService().update(cytoscapeJson)
                                        MilkyWayToolWindowOpener.open(project)
                                    }
                                } catch (exception: Exception) {
                                    ApplicationManager.getApplication().invokeLater {
                                        showErrorDialog(
                                            project,
                                            exception.message ?: "Unknown error",
                                            "MilkyWay Dependency Analysis Failed"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}