package com.github.milkyway.gradle

import com.github.milkyway.core.MilkyWayConstants
import com.github.milkyway.gradle.tasks.PrintDependenciesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class MilkyWayPlugin : Plugin<Project> {

    private companion object {
        const val REPORTS_DIR = "reports/milkyway"
    }

    override fun apply(project: Project) {
        project.tasks.register(
            MilkyWayConstants.GRADLE_ANALYSIS_TASK_NAME,
            PrintDependenciesTask::class.java
        ) {
            it.group = "milkyway"
            it.description = "Analyze project module dependency graph"
            it.outputDir.set(project.layout.buildDirectory.dir(REPORTS_DIR))
        }
    }
}