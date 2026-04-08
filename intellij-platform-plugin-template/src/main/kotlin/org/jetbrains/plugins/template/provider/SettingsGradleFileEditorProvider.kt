package org.jetbrains.plugins.template.provider

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.template.editor.SettingsGradleSplitEditor

class SettingsGradleFileEditorProvider : FileEditorProvider, DumbAware {
    private val searchFiles = listOf("settings.gradle", "settings.gradle.kts")
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.name in searchFiles
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return SettingsGradleSplitEditor(project, file)
    }

    override fun getEditorTypeId(): @NonNls String = "settings-gradle-graph-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}