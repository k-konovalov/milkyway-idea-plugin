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
rootProject.name = "rectangle-20-gradle_8_14_4-shape-test-project"

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
include (":feature:checkout")
include (":feature:cart")
include (":feature:user")
include (":feature:profile")
include (":domain:feed")
include (":domain:comment")
include (":domain:post")
include (":domain:search")
include (":repository:location")
include (":repository:network")
include (":repository:sync")
include (":repository:share")
include (":model:group")
include (":model:message")
include (":model:notification")
include (":model:setting")
include (":app:app")