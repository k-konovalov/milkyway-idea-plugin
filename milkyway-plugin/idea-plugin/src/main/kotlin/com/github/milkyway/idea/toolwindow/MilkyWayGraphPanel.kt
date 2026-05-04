package com.github.milkyway.idea.toolwindow

import com.github.milkyway.idea.cytoscape.HtmlRenderer
import com.github.milkyway.idea.milkyWayReportService
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MilkyWayGraphPanel(
    project: Project
) : JPanel(BorderLayout()) {

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
        val browser = JBCefBrowser()
        browser.loadHTML(HtmlRenderer.render(json))
        return browser.component
    }
}