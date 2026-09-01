package com.github.milkyway.idea.presentation.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls

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
        val startupGraphAnalysis = StartupGraphAnalysis(project, file, previewEditor)
        startupGraphAnalysis.run()
        return splitEditor
    }

    override fun getEditorTypeId(): @NonNls String = "milkyway-split-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
