package com.github.milkyway.parser.api

import com.github.milkyway.core.models.DependencyGraph

interface DependencyResolver {
    fun resolve(): DependencyGraph
}
