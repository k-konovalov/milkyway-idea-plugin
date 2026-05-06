package com.github.milkyway.core.shape

import com.github.milkyway.core.models.Shape

class ShapeMatcher(
    private val targetLength: Int = 32,
    private val shapeManager: ShapeManager = ShapeManager()
) {
    private val matchedShapes = Shape.getAllShapes()
    fun calculate(initVec: List<Int>): Map<Shape, Double> {
        val result = mutableMapOf<Shape, Double>()
        val vec = initVec.map { it.toDouble() }
        for (matchedShape in matchedShapes) {
            val matchedProfile = matchedShape
                .getBaseProfile(targetLength)
                .map { it.toDouble() }

            println("--------------")
            println("--------------")
            println("Shape: ")
            println(Shape.describe(matchedShape))
            println("--------------")
            println("--------------")
            println("Match Profile: ")
            println(matchedProfile)

            val similarity = shapeManager.similarityPercent(vec, matchedProfile, targetLength)
            println("--------------")
            println("--------------")
            println("Similarity: ")
            println(similarity)
            result[matchedShape] = similarity
        }

        return result
    }
}
