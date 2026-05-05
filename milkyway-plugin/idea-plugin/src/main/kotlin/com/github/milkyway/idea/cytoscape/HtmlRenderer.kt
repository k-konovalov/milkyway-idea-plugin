package com.github.milkyway.idea.cytoscape

object HtmlRenderer {

    fun render(cytoscapeJson: String): String {
        val html = loadResource("/web/cytoscape.html")
        val css = loadResource("/web/cytoscape.css")
        val cytoscapeJs = loadResource("/web/cytoscape.min.js")
        val viewJs = loadResource("/web/cytoscape-view.js")

        val safeJson = cytoscapeJson.replace("</script>", "<\\/script>")

        return html
            .replace("{{MILKYWAY_CSS}}", css)
            .replace("{{CYTOSCAPE_JS}}", cytoscapeJs)
            .replace("{{MILKYWAY_REPORT_JSON}}", safeJson)
            .replace("{{MILKYWAY_VIEW_JS}}", viewJs)
    }

    private fun loadResource(path: String): String {
        return HtmlRenderer::class.java
            .getResourceAsStream(path)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Resource not found: $path")
    }
}