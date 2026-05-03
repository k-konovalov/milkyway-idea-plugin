package com.github.milkyway.plugin.services.shape

import com.github.milkyway.plugin.model.DependencyGraph
import com.github.milkyway.plugin.model.Shape

class GraphShapeMatcher(
    private val shapeMatcher: ShapeMatcher = ShapeMatcher()
) {
    fun calculate(dependencyGraph: DependencyGraph): Map<Shape, Double> {
        val graphProfile = dependencyGraph.buildLayerProfile()
        println("--------------")
        println("--------------")
        println("Graph Profile: ")
        println(graphProfile)
        val report = shapeMatcher.calculate(graphProfile)
        return report
    }
}
