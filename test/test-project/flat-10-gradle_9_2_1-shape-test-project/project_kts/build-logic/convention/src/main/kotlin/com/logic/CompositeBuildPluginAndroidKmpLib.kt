package com.logic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CompositeBuildPluginAndroidKmpLib : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.kotlin.multiplatform.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.google.devtools.ksp")
                
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                    namespace = "com.awesomeapp." + target.name.replace(":","_").replace("-", "")
                    compileSdk = 36
                    minSdk = 24
                    withHostTestBuilder {}
                    withDeviceTestBuilder {
                        sourceSetTreeName = "test"
                    }.configure {
                        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    }
                    withJava()
                    androidResources.enable = true
                }

                jvmToolchain(23)
            }

            dependencies {

            }

            tasks.matching { it.name == "extractAndroidMainAnnotations" }.configureEach {
    dependsOn("kspAndroidMain")
}
        }
    }
}
