plugins {
    alias(libs.plugins.kotlinBase)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.github.milkyway.visualizer.cytoscape"


dependencies {
    implementation(libs.kotlinxSerializationJson)
}
