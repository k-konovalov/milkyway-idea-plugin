package com.github.milkyway.plugin.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VfsUtilCore
import com.github.milkyway.plugin.services.MyProjectService

class GradleReaderStartupActivity : ProjectActivity {

    init {
        thisLogger().debug("Init GradleReaderStartupActivity")
    }
    override suspend fun execute(project: Project) {
        thisLogger().debug("project.baseDir: " + project.baseDir)
        val service = project.service<MyProjectService>()
        service.printSettingsGradleFiles()
    }
}