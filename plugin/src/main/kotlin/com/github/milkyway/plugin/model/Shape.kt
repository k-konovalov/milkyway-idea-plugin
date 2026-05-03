package com.github.milkyway.plugin.model

/**
 * Flat shape is not supported because there is no relevant distribution for flat shape
 */
enum class Shape {
    TRIANGLE {
        override fun getBaseProfile(layers: Int): List<Int> {
            return (1..layers).toList()
        }
    },
    INVERSE_TRIANGLE {
        override fun getBaseProfile(layers: Int): List<Int> {
            return (layers downTo 1).toList()
        }
    },
    RECTANGLE {
        override fun getBaseProfile(layers: Int): List<Int> {
            return List(layers) { 1 }
        }
    },
    RHOMBUS {
        override fun getBaseProfile(layers: Int): List<Int> {
            val left = (1..(layers / 2 + 1)).toList()
            val right = if (layers % 2 == 0) {
                left.reversed()
            } else {
                left.dropLast(1).reversed()
            }
            return left + right
        }
    },
    MIDDLE_BOTTLENECK {
        override fun getBaseProfile(layers: Int): List<Int> {
            val left = (layers downTo (layers / 2 + 1)).toList()
            val right = if (left.size % 2 == 0) {
                left.reversed()
            } else {
                left.dropLast(1).reversed()
            }
            return left + right
        }
    };

    abstract fun getBaseProfile(layers: Int): List<Int>;

    companion object {
        fun describe(shape: Shape): String = when(shape) {
            TRIANGLE -> "TRIANGLE"
            INVERSE_TRIANGLE -> "INVERSE_TRIANGLE"
            RECTANGLE -> "RECTANGLE"
            RHOMBUS -> "RHOMBUS"
            MIDDLE_BOTTLENECK -> "MIDDLE_BOTTLENECK"
        }

        fun getAllShapes(): List<Shape> = listOf(
            TRIANGLE,
            INVERSE_TRIANGLE,
            RECTANGLE,
            RHOMBUS,
            MIDDLE_BOTTLENECK,
        )
    }
}