package org.jetbrains.plugins.template.editor

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

class SettingsGradlePreviewEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {
    private val panel = JPanel(BorderLayout())
    private val label = JLabel("Dependency Graph Label")
    private val scrollPane = JScrollPane(label)

    private val document =
        FileDocumentManager.getInstance().getDocument(file)
            ?: error("Can not get document for file")

    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            updatePreview(document.text)
        }
    }

    init {
        panel.add(scrollPane, BorderLayout.CENTER)
        document.addDocumentListener(documentListener)
    }

    // TODO: Add File Parsing and Building Graph
    private fun updatePreview(text: String) {
        label.text = "<html><pre>${text}</pre></html>"
    }

    override fun getComponent() = panel

    override fun getPreferredFocusedComponent() = panel

    override fun getName() = "Dependency Graph Editor Name"

    override fun setState(state: FileEditorState) {}

    override fun isModified() = false

    override fun isValid() = true

    override fun addPropertyChangeListener(p0: PropertyChangeListener) {}

    override fun removePropertyChangeListener(p0: PropertyChangeListener) {}

    override fun dispose() {
        document.removeDocumentListener(documentListener)
    }
}