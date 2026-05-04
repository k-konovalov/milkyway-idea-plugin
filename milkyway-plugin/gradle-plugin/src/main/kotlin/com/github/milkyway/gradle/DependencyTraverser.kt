package com.github.milkyway.gradle

import com.github.milkyway.core.models.DependencyGraph
import org.gradle.api.Project

interface DependencyTraverser {

    fun traverse(project: Project, graph: DependencyGraph)

}