package com.github.milkyway.core.resolver

import com.github.milkyway.core.models.DependencyGraph

interface DependencyResolver {
    fun resolve(): DependencyGraph
}
