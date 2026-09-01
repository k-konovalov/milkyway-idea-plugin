package com.github.milkyway.idea.presentation.toolwindow

import com.github.milkyway.idea.data.repository.milkyWayReportService
import com.github.milkyway.idea.platform.settings.VisualizerSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MilkyWayGraphPanel(
    project: Project
) : JPanel(BorderLayout()), Disposable {
    private var browser: JBCefBrowser = JBCefBrowser()
    private val settings = VisualizerSettings.getInstance()

    init {
        val cachedJson = project.milkyWayReportService().loadCached()
        reload(cachedJson)
    }

    fun reload(html: String?) {
        removeAll()

        if (html == null) {
            add(JLabel("Run Tools → Analyze Gradle Dependencies first."), BorderLayout.CENTER)

            revalidate()
            repaint()
            return
        }

        add(createGraphPanel(html), BorderLayout.CENTER)

        revalidate()
        repaint()
    }

    private fun createGraphPanel(html: String): JComponent {
        browser.loadHTML(html)

        if (settings.state.isDevToolsEnabled) {
            SwingUtilities.invokeLater {
                browser.openDevtools()
            }
        }
        return browser.component
    }

    override fun dispose() {
        browser.dispose()
    }
}
