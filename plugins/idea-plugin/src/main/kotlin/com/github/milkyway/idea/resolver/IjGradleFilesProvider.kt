package com.github.milkyway.idea.resolver

import com.github.milkyway.parser.regex.GradleFilesProvider
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

/**
 * Adapter implementing the [GradleFilesProvider] port via the IntelliJ VFS.
 */
class IjGradleFilesProvider(
    private val project: Project,
) : GradleFilesProvider {
    override fun settingsContent(): String? {
        return project.baseDir.findChild("settings.gradle.kts")?.getLatestText()
    }

    override fun buildFiles(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        VfsUtilCore.visitChildrenRecursively(project.baseDir, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.name == "build.gradle.kts" &&
                    !file.path.contains("/build/") &&
                    !file.path.contains("/.gradle/")
                ) {
                    val relativePath = VfsUtilCore.getRelativePath(file, project.baseDir)
                    if (relativePath != null) {
                        result.add(relativePath to file.getLatestText())
                    }
                }
                return true
            }
        })
        return result
    }

    private fun VirtualFile.getLatestText(): String {
        val document = FileDocumentManager.getInstance().getCachedDocument(this)
        return document?.text ?: String(this.contentsToByteArray(), Charsets.UTF_8)
    }
}
