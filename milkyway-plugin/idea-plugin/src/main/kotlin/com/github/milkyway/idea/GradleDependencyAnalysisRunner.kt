package com.github.milkyway.idea

import com.github.milkyway.core.MilkyWayConstants
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class GradleDependencyAnalysisRunner(
    private val ideaProject: Project,
) {

    private companion object {
        const val REPORT_DIR = "build/reports/milkyway"
        const val MILKYWAY_INIT_GRADLE_FILE = "milkyway-init.gradle"
    }

    fun run(projectDir: File): String {
        val reportDir = File(projectDir, REPORT_DIR)
        val cytoscapeFile = reportDir.resolve(MilkyWayConstants.CYTOSCAPE_REPORT_FILE)
        val initScript = createInitScript()

        if (reportDir.exists()) {
            reportDir.deleteRecursively()
        }

        try {
            runDependencyAnalysis(projectDir, initScript)

            if (!cytoscapeFile.exists()) {
                error("Cytoscape file was not generated: ${cytoscapeFile.absolutePath}")
            }

            return cytoscapeFile.readText()
        } finally {
            initScript.delete()
        }
    }

    private fun createInitScript(): File {
        val input = javaClass.classLoader
            .getResourceAsStream(MILKYWAY_INIT_GRADLE_FILE)
            ?: error("$MILKYWAY_INIT_GRADLE_FILE not found")

        return Files.createTempFile("milkyway-init-", ".gradle").toFile().also { file ->
            input.use { source ->
                file.outputStream().use { target ->
                    source.copyTo(target)
                }
            }
        }
    }

    private fun runDependencyAnalysis(projectDir: File, initScript: File) {
        val failure = AtomicReference<Throwable?>()
        val latch = CountDownLatch(1)

        val settings = ExternalSystemTaskExecutionSettings().apply {
            externalSystemIdString = GradleConstants.SYSTEM_ID.id
            externalProjectPath = projectDir.absolutePath

            taskNames = listOf(":" + MilkyWayConstants.GRADLE_ANALYSIS_TASK_NAME)

            scriptParameters = buildString {
                append("--init-script ")
                append(quote(initScript.absolutePath))
                append(" --stacktrace")
                append(" --info")
            }

            vmOptions = buildString {
                append("-Didea.home.path=")
                append(quote(PathManager.getHomePath()))
            }
        }

        val callback = object : TaskCallback {
            override fun onSuccess() {
                latch.countDown()
            }

            override fun onFailure() {
                failure.set(RuntimeException("Gradle task failed"))
                latch.countDown()
            }
        }

        ExternalSystemUtil.runTask(
            settings,
            DefaultRunExecutor.EXECUTOR_ID,
            ideaProject,
            GradleConstants.SYSTEM_ID,
            callback,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC,
            true,
        )

        latch.await()

        failure.get()?.let { throw it }
    }

    private fun quote(value: String): String {
        return "\"${value.replace("\"", "\\\"")}\""
    }
}