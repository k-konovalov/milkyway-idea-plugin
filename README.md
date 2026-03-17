# Milkyway
IDEA plugin for visualise Gradle module dependencies as a graph on demand during build.gradle editing.

## Main idea
Mobile developers, when editing build.gradle.kts, specify the dependencies between modules to link them and use the imports/classes of the corresponding connected module.  
Over time, these dependencies become quite deep — so deep that they start affecting the build time of the entire project.  
An Milkyway plugin parses build.gradle.kts and displays a graph of module dependencies in real time. 

## Contribution
Feel free for creating Issues and Pull Requests. See [CONTRIBUTE.md](CONTRIBUTE.md) for details

## Thanks for
- All contributors: past and future
- [laniake-gradle-plugin](https://github.com/inDriver/laniakea-gradle-plugin): for gradle ideas
- [module-graph-assert](https://github.com/jraska/modules-graph-assert): for critical paths ideas
- [lobzik](https://github.com/Mishkun/lobzik]): for metricks (decoupling, critical path)
- [ProjectGenerator](https://github.com/cdsap/ProjectGenerator): for test project
