pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("com.fueledbycaffeine.spotlight") version "1.4.1"
}
rootProject.name = "triangle-20-gradle_9_4_1-shape-test-project"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
} 
include (":core:push")
include (":core:contact")
include (":core:login")
include (":core:identity")
include (":core:checkout")
include (":core:cart")
include (":core:user")
include (":core:profile")
include (":core:feed")
include (":feature:comment")
include (":feature:post")
include (":feature:search")
include (":feature:location")
include (":feature:network")
include (":domain:sync")
include (":domain:share")
include (":domain:group")
include (":repository:message")
include (":repository:notification")
include (":model:setting")
include (":app:app")