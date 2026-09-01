package com.github.milkyway.idea.editor

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MilkywaySplitEditor(
    project: Project,
    file: VirtualFile,
    previewEditor: MilkywayPreviewEditor
): TextEditorWithPreview(
    TextEditorProvider.getInstance().createEditor(project, file) as TextEditor,
    previewEditor,
    "Milkyway",
)
