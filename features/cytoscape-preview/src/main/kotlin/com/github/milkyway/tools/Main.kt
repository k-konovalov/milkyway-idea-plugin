package com.github.milkyway.tools

import com.github.milkyway.graph.GraphAnalysisRunner
import com.github.milkyway.parser.regex.FileSystemGradleFilesProvider
import com.github.milkyway.parser.regex.RegexGradleParser
import com.github.milkyway.visualizer.cytoscape.CytoscapePluginSettingsDto
import java.io.File

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Usage: main <projectRootPath>" }
    val projectRoot = File(args[0]).canonicalFile
    require(projectRoot.isDirectory) { "Not a directory: $projectRoot" }

    val filesProvider = FileSystemGradleFilesProvider(projectRoot)
    val resolver = RegexGradleParser(filesProvider)
    val html = GraphAnalysisRunner(
        resolver = resolver,
        moduleName = null,
        cytoscapeSettings = CytoscapePluginSettingsDto(),
    ).run()

    val outputDir = File(projectRoot, "build/output")
    outputDir.mkdirs()
    val outputFile = File(outputDir, "cytoscape.html")
    outputFile.writeText(html)

    println("Written: $outputFile")
}
