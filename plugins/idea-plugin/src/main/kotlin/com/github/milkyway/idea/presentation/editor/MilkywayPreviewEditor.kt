package com.github.milkyway.idea.presentation.editor

import com.github.milkyway.idea.platform.settings.VisualizerSettings
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.Alarm
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MilkywayPreviewEditor(
    private val project: Project,
    private val file: VirtualFile,
): UserDataHolderBase(), FileEditor {
    private val panel = JPanel(BorderLayout())
    private val browser = JBCefBrowser()
    private val settings = VisualizerSettings.getInstance()
    private val document = FileDocumentManager.getInstance().getDocument(file) ?:
        error("Can't get document for file ${file.path}")
    private var dependencySet: Set<String> = mutableSetOf()
    private var includeSet: Set<String> = mutableSetOf()

    private var alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            alarm.cancelAllRequests()

            val text = event.document.text
            val newDependencySet = parseModuleDeps(text)
            val newIncludeSet = parseSettingsDeps(text)

            if (newDependencySet == dependencySet && newIncludeSet == includeSet) {
                return
            }
            dependencySet = newDependencySet
            includeSet = newIncludeSet
            alarm.addRequest({
                val startupGraphAnalysis = StartupGraphAnalysis(project, file, this@MilkywayPreviewEditor)
                startupGraphAnalysis.run()
            }, 1000)
        }
    }

    init {
        dependencySet = parseModuleDeps(file.readText())
        includeSet = parseSettingsDeps(file.readText())
        reload(null)
        panel.add(browser.component, BorderLayout.CENTER)
        document.addDocumentListener(documentListener)
    }

    fun reload(html: String?) {
        if (html == null) {
            browser.loadHTML("<html><body>Run Tools → Analyze Gradle Dependencies first.</body></html>")
            return
        }
        browser.loadHTML(html)
        if (settings.state.isDevToolsEnabled) {
            SwingUtilities.invokeLater {
                browser.openDevtools()
            }
        }
    }

    // region Mostly unused
    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent? = panel

    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String = "Milkyway Editor"

    override fun setState(state: FileEditorState) { }

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(p0: PropertyChangeListener) { }

    override fun removePropertyChangeListener(p0: PropertyChangeListener) { }
    // endregion

    // region Parse all dependencies
    companion object {
        private val includeRegex = Regex("""include\s*\(["'](:[^"']+)["']\s*\)""")
        private val dependencyRegex = Regex(
            """(\w+)\s*\(\s*project\s*\(\s*["'](:[^"']+)["']\s*\)\s*\)""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )
    }

    private fun parseSettingsDeps(settingsText: String): Set<String> {
        return includeRegex.findAll(settingsText)
            .map { it.groupValues[1] }
            .toSet()
    }

    private fun parseModuleDeps(buildText: String): Set<String> {
        return dependencyRegex.findAll(buildText)
            .map { it.groupValues[2] }.toSet()
    }

    // endregion
    override fun dispose() {
        println("__Disposing preview editor ${file.path}")
        document.removeDocumentListener(documentListener)
        Disposer.dispose(browser)
    }
}
