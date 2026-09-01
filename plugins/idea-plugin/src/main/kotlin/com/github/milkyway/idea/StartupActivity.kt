package com.github.milkyway.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class StartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        GradleFileListener(project)
    }
}
