package com.github.milkyway.plugin.model

data class Node (val name: String) {
    val neighbours: MutableList<Node> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Node) {
            return false
        }
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }
}