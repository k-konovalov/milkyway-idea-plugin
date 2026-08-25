plugins {
    id("awesome.androidlib.plugin")
}

dependencies {
    implementation(project(":repository:post"))
    implementation(project(":repository:feed"))
    implementation(project(":repository:profile"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.coroutines.android)
    implementation(libs.fragment.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.work.runtime.ktx)
    
    implementation(libs.hilt.work)
    implementation(libs.hilt.android)
    
    add("ksp", libs.hilt.compiler.androidx)
    add("ksp", libs.hilt.compiler)
    add("ksp", libs.kotlin.jvm.metadata)
    add("kspTest", libs.hilt.compiler)
    add("kspAndroidTest", libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    
    
    implementation(project.dependencies.platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    
    testImplementation(libs.junit4)
    testImplementation(libs.junit5.vintage)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.core.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.work.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
    

}