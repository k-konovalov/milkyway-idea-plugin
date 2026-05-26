package com.github.milkyway.idea.toolwindow

import com.github.milkyway.idea.cytoscape.HtmlRenderer
import com.github.milkyway.idea.milkyWayReportService
import com.github.milkyway.idea.settings.MilkyWaySettings
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
    private val settings = MilkyWaySettings.getInstance()

    init {
        val cachedJson = project.milkyWayReportService().loadCached()
        reload(cachedJson)
    }

    fun reload(json: String?) {
        removeAll()

        if (json == null) {
            add(JLabel("Run Tools → Analyze Gradle Dependencies first."), BorderLayout.CENTER)

            revalidate()
            repaint()
            return
        }

        add(createGraphPanel(json), BorderLayout.CENTER)

        revalidate()
        repaint()
    }

    private fun createGraphPanel(json: String): JComponent {
        browser.loadHTML(HtmlRenderer.render(json))

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
