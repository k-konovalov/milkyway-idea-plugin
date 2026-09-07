package com.github.milkyway.parser.regex

import java.io.File

/**
 * Filesystem-backed implementation of [GradleFilesProvider].
 *
 * Walks a given [rootDir] and reads `settings.gradle(.kts)` and every
 * `build.gradle(.kts)` file found within the directory tree.
 */
class FileSystemGradleFilesProvider(
    private val rootDir: File,
) : GradleFilesProvider {

    override fun settingsContent(): String? {
        val settingsKts = File(rootDir, "settings.gradle.kts")
        if (settingsKts.isFile) {
            return settingsKts.readText()
        }
        val settingsGroovy = File(rootDir, "settings.gradle")
        if (settingsGroovy.isFile) {
            return settingsGroovy.readText()
        }
        return null
    }

    override fun buildFiles(): List<Pair<String, String>> {
        val buildFileNames = setOf("build.gradle.kts", "build.gradle")
        val result = mutableListOf<Pair<String, String>>()

        rootDir.walkTopDown()
            .filter { it.isFile && it.name in buildFileNames }
            .forEach { file ->
                val relativePath = rootDir.toPath()
                    .relativize(file.toPath())
                    .toString()
                result.add(relativePath to file.readText())
            }

        return result
    }
}
