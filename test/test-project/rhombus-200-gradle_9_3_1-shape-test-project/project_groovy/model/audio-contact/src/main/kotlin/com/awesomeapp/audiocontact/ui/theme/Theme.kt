package com.awesomeapp.audiocontact.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme // Or darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    // Define basic colors or leave defaults
)

@Composable
fun FeatureTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}