package com.github.milkyway.plugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore

@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {

    fun getRandomNumber() = (1..100).random()

    fun printSettingsGradleFiles() {
        thisLogger().debug("project.baseDir: " + project.baseDir)

        val baseDir = project.baseDir ?: return
        val searchFiles = listOf("build.gradle.kts", "settings.gradle.kts")
        VfsUtilCore.iterateChildrenRecursively(baseDir, null) { file ->
            if (file.name in searchFiles) {
                println(file.canonicalPath)
            }
            true
        }
    }
}
