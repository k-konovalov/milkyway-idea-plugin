package com.github.milkyway.algorithm.shapematching

internal class ShapeMatcher(
    private val targetLength: Int = 32,
    private val shapeManager: ShapeManager = ShapeManager(),
) {
    private val matchedShapes = Shape.getAllShapes()

    fun calculate(initVec: List<Int>): Map<Shape, Double> {
        val result = mutableMapOf<Shape, Double>()
        val vec = initVec.map { it.toDouble() }
        for (matchedShape in matchedShapes) {
            val matchedProfile = matchedShape
                .getBaseProfile(targetLength)
                .map { it.toDouble() }

            val similarity = shapeManager.similarityPercent(vec, matchedProfile, targetLength)
            result[matchedShape] = similarity
        }

        return result
    }
}
