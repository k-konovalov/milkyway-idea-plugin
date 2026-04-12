package com.github.milkyway

import com.github.milkyway.models.DependencyGraph
import org.gradle.api.Project

interface DependencyTraverser {

    fun traverse(project: Project, graph: DependencyGraph)

}