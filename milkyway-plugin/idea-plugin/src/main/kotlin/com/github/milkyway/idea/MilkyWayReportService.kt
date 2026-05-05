package com.github.milkyway.idea

import com.github.milkyway.core.MilkyWayConstants
import com.github.milkyway.idea.toolwindow.MilkyWayGraphPanel
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

@Service(Service.Level.PROJECT)
class MilkyWayReportService(
    private val project: Project
) {

    var latestCytoscapeJson: String? = null
        private set

    var panel: MilkyWayGraphPanel? = null

    fun update(json: String) {
        latestCytoscapeJson = json
        saveCached(json)
        panel?.reload(json)
    }

    fun loadCached(): String? {
        latestCytoscapeJson?.let {
            return it
        }

        val file = cacheFile()

        if (!Files.exists(file)) {
            return null
        }

        val json = Files.readString(file)
        latestCytoscapeJson = json

        return json
    }

    private fun saveCached(json: String) {
        val file = cacheFile()

        Files.createDirectories(file.parent)
        Files.writeString(file, json)
    }

    private fun cacheFile(): Path {
        val basePath = project.basePath ?: project.name
        val projectHash = sha256(basePath).take(16)

        return PathManager.getSystemDir().resolve("milkyway").resolve(projectHash)
            .resolve(MilkyWayConstants.CYTOSCAPE_REPORT_FILE)
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
}

fun Project.milkyWayReportService(): MilkyWayReportService {
    return service()
}