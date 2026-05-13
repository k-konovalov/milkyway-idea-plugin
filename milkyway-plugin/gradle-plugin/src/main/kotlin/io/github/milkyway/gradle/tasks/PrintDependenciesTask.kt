package io.github.milkyway.gradle.tasks

import com.github.milkyway.core.MilkyWayConstants
import com.github.milkyway.core.mapper.GraphDependencyMapper
import com.github.milkyway.core.models.DependencyGraph
import io.github.milkyway.gradle.traversers.ProjectModulesTraverser
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class PrintDependenciesTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun printAll() {
        val dir = outputDir.get().asFile

        cleanOutputDir(dir)

        val graph = DependencyGraph()
        val traverser = ProjectModulesTraverser()

        for (subproject in project.rootProject.allprojects) {
            traverser.traverse(subproject, graph)
        }

        val graphDto = GraphDependencyMapper.toDto(graph)

        val graphFile = dir.resolve(MilkyWayConstants.GRADLE_DEPENDENCY_GRAPH_FILE)
        val text = Json {
            prettyPrint = true
            encodeDefaults = true
        }.encodeToString(graphDto)

        graphFile.writeText(text)
    }

    private fun cleanOutputDir(dir: File) {
        if (dir.exists() && !dir.isDirectory) {
            dir.delete()
        }
        dir.mkdirs()
    }

}