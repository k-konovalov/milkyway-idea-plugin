package com.github.milkyway.idea.presentation.toolwindow

import com.github.milkyway.idea.data.repository.milkyWayReportService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class MilkyWayToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MilkyWayGraphPanel(project)

        project.milkyWayReportService().panel = panel

        val content = toolWindow.contentManager.factory.createContent(
            panel,
            "Graph",
            false
        )

        toolWindow.contentManager.addContent(content)
    }
}
