package com.github.milkyway.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.github.milkyway.plugin.services.MyProjectService

class BuildGraphAction : AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project: Project = actionEvent.project ?: return
        val service = project.service<MyProjectService>()
        service.printSettingsGradleFiles()
    }
}