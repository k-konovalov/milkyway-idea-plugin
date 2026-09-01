package com.github.milkyway.parser.regex

/**
 * Port providing raw Gradle file contents to [RegexGradleParser].
 *
 * Keeps the parser free of any IntelliJ Platform dependency (Clean Architecture):
 * the IDEA plugin supplies an implementation backed by the VFS.
 */
interface GradleFilesProvider {
    fun settingsContent(): String?

    /** @return list of (relativePath, content) pairs for every build file */
    fun buildFiles(): List<Pair<String, String>>
}
