package com.github.milkyway.plugin.services.shape

import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.max
import kotlin.math.min

class ShapeManager {
    fun normalize(vec: List<Double>): List<Double> {
        val sum = vec.sum()
        return if (sum == 0.0) {
            vec
        } else {
            vec.map { it / sum }
        }
    }

    /**
     * Apply linear interpolation to vector, to make it different size
     * Necessary when compare cosine similarity (vec sizes must be equal)
     *
     * y(x) = y_0 + (x - x_0)/(x_1 - x_0) * (y_1 - y_0)
     * t = (x - x_0)(x_1 - x_0)
     * y(x) = (1 - t) * y_0 + t * y_1
     */
    fun resampleLinear(vec: List<Double>, targetLength: Int): List<Double> {
        if (vec.size == targetLength) {
            return vec.toList()
        }
        if (vec.size == 1) {
            return List(targetLength) { vec[0] }
        }
        val result = mutableListOf<Double>()
        val last = vec.size - 1
        for (i in 0 until targetLength) {
            val x = i * last.toDouble() / (targetLength - 1)
            val x0 = floor(x).toInt()
            val x1 = min(x0 + 1, last)
            val t = x - x0

            /**
             * if t == 0, then y = vec[x_0]
             * if t == 1, then y = vec[x_1]
             * if t in (0, 1) return mean
             */
            val y = vec[x0] * (1 - t) + vec[x1] * t
            result.add(y)
        }
        return result
    }

    /**
     * sim(a, b) = 100 * (a * b) / (||a|| * ||b||)
     */
    fun cosineSimilarity(a: List<Double>, b: List<Double>): Double {
        val size = min(a.size, b.size)
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in 0 until size) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val da = sqrt(normA)
        val db = sqrt(normB)
        if (da == 0.0 || db == 0.0) {
            return 0.0
        }
        return dot / (da * db)
    }

    fun similarityPercent(
        profileA: List<Double>,
        profileB: List<Double>,
        targetLen: Int = 32
    ): Double {
        val resampledA = resampleLinear(profileA, targetLen)
        val resampledB = resampleLinear(profileB, targetLen)
        val normalizedA = normalize(resampledA)
        val normalizedB = normalize(resampledB)
        val score = cosineSimilarity(normalizedA, normalizedB)
        return max(0.0, min(100.0, 100.0 * score))
    }
}