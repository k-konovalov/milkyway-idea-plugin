package com.github.milkyway.idea.data.source

import com.github.milkyway.core.models.DependencyGraph
import com.github.milkyway.parser.api.DependencyResolver
import com.github.milkyway.parser.regex.RegexGradleParser
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project

class RegexDependencyResolver(
    private val project: Project,
) : DependencyResolver {
    override fun resolve(): DependencyGraph =
        ReadAction.compute<DependencyGraph, Throwable> {
            RegexGradleParser(IjGradleFilesProvider(project)).resolve()
        }
}
