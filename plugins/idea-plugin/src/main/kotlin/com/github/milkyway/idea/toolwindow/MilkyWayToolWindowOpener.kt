package com.github.milkyway.idea.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

object MilkyWayToolWindowOpener {
    fun open(project: Project) {
        val toolWindow = ToolWindowManager.Companion
            .getInstance(project)
            .getToolWindow("MilkyWay")
            ?: return

        toolWindow.show()
    }
}