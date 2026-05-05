package com.github.milkyway.core.contract.traverser

import com.github.milkyway.core.models.DependencyGraph

interface DependencyTraverser {
    fun traverse(): DependencyGraph
}