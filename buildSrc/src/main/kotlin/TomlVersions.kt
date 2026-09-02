import java.io.File

object TomlVersions {
    private val VERSION_REGEX = """^\s*(\w[\w-]*)\s*=\s*"([^"]+)"""".toRegex()
    private const val VERSIONS_FILEPATH = "gradle/libs.versions.toml"

    fun parse(rootDir: File): Map<String, String> =
        rootDir.resolve(VERSIONS_FILEPATH)
            .readLines()
            .mapNotNull { line ->
                VERSION_REGEX.find(line)
                    ?.destructured
                    ?.let { (k, v) -> k to v }
            }
            .toMap()
}
