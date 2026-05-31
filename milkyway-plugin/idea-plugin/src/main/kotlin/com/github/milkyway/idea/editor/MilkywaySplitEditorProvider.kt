package com.github.milkyway.idea.editor

import com.github.milkyway.idea.GradleDependencyAnalysisRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls
import java.io.File

class MilkywaySplitEditorProvider: FileEditorProvider, DumbAware {
    private companion object {
        val SEARCH_FILES = listOf("settings.gradle.kts", "build.gradle.kts")
    }
    override fun accept(
        project: Project,
        file: VirtualFile
    ): Boolean {
        return file.name in SEARCH_FILES
    }

    override fun createEditor(
        project: Project,
        file: VirtualFile
    ): FileEditor {
        val previewEditor = MilkywayPreviewEditor(project, file)
        val splitEditor = MilkywaySplitEditor(project, file, previewEditor)
        ProgressManager.getInstance().run(
            object: Task.Backgroundable(
                project, "Analyzing Gradle dependencies", true
            ) {
                override fun run(indicator: ProgressIndicator) {
                    val basePath = project.basePath ?: return
                    val projectDir = File(basePath)
                    try {
                        indicator.text = "Running Gradle analysis"
                        val cyJson = GradleDependencyAnalysisRunner(project, srcGradleFile = file).run(projectDir)
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
        return splitEditor
    }

    override fun getEditorTypeId(): @NonNls String = "milkyway-split-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
