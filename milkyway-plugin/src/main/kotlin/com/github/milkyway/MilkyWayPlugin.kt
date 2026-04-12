package com.github.milkyway

import com.github.milkyway.tasks.PrintDependenciesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class MilkyWayPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register(
            "printDependencies",
            PrintDependenciesTask::class.java
        ) {}
    }

}