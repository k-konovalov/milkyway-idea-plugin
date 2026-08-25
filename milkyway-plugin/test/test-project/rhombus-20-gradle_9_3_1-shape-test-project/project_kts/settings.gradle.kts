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
rootProject.name = "rhombus-20-gradle_9_3_1-shape-test-project"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
} 
include (":core:push")
include (":core:contact")
include (":feature:login")
include (":feature:identity")
include (":feature:checkout")
include (":feature:cart")
include (":feature:user")
include (":domain:profile")
include (":domain:feed")
include (":domain:comment")
include (":domain:post")
include (":domain:search")
include (":repository:location")
include (":repository:network")
include (":repository:sync")
include (":repository:share")
include (":repository:group")
include (":model:message")
include (":model:notification")
include (":app:app")