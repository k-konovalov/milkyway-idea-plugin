package com.github.milkyway.idea

import com.github.milkyway.core.MilkyWayConstants
import com.github.milkyway.core.mapper.GraphDependencyMapper
import com.github.milkyway.core.models.DependencyGraphDto
import com.github.milkyway.idea.cytoscape.ReportBuilder
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.Json
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

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun run(projectDir: File): String {
        val reportDir = File(projectDir, REPORT_DIR)
        val graphFile = reportDir.resolve(MilkyWayConstants.GRADLE_DEPENDENCY_GRAPH_FILE)
        val initScript = createInitScript()

        if (reportDir.exists()) {
            reportDir.deleteRecursively()
        }

        try {
            runDependencyAnalysis(projectDir, initScript)

            if (!graphFile.exists()) {
                error("Dependency graph file was not generated: ${graphFile.absolutePath}")
            }

            val graphDto = json.decodeFromString<DependencyGraphDto>(graphFile.readText())
            val graph = GraphDependencyMapper.fromDto(graphDto)
            val cytoscapeReport = ReportBuilder().build(graph)

            return json.encodeToString(cytoscapeReport)
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