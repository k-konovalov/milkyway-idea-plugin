package com.github.milkyway.idea.resolver

import com.github.milkyway.core.models.DependencyGraph

interface DependencyResolver {
    fun resolve(): DependencyGraph
}