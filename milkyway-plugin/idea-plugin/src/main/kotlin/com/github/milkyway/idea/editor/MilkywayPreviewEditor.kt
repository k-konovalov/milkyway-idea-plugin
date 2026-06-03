package com.github.milkyway.idea.editor

import com.github.milkyway.idea.cytoscape.HtmlRenderer
import com.github.milkyway.idea.settings.MilkyWaySettings
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefBrowser
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MilkywayPreviewEditor(
    private val project: Project,
): UserDataHolderBase(), FileEditor {
    private val panel = JPanel(BorderLayout())
    private val browser = JBCefBrowser()
    private val settings = MilkyWaySettings.getInstance()

    init {
        reload(null)
        panel.add(browser.component, BorderLayout.CENTER)
    }

    fun reload(json: String?) {
        if (json == null) {
            browser.loadHTML("<html><body>Run Tools → Analyze Gradle Dependencies first.</body></html>")
            return
        }
        browser.loadHTML(HtmlRenderer.render(json))
        if (settings.state.isDevToolsEnabled) {
            SwingUtilities.invokeLater {
                browser.openDevtools()
            }
        }
    }

//    private val documentListener = object : DocumentListener {
//        override fun documentChanged(event: DocumentEvent) {
//            updatePreview();
//        }
//    }

    private fun updatePreview() {
    }
    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent? = panel

    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String = "Milkyway Editor"

    override fun setState(state: FileEditorState) { }

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(p0: PropertyChangeListener) { }

    override fun removePropertyChangeListener(p0: PropertyChangeListener) { }

    override fun dispose() {
//        document.removeDocumentListener(documentListener)
    }
}
