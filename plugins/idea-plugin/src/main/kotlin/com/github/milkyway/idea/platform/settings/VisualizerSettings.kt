package com.github.milkyway.idea.platform.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(
    name = "MilkyWayVisualizerSettings",
    storages = [Storage("milkyway-visualizer.xml")]
)
class VisualizerSettings : PersistentStateComponent<VisualizerSettings.State> {
    companion object {
        const val THEME_BLACK = "Black"
        const val THEME_WHITE = "White"
        const val THEME_COLORED = "Colored"
        fun getInstance(): VisualizerSettings = service()
    }

    data class State(
        var theme: String = THEME_BLACK,
        var isGroupingByRegionEnabled: Boolean = false,
        var isDevToolsEnabled: Boolean = false,
        var isAnimationEnabled: Boolean = false,
        var isWebGlEnabled: Boolean = true,
        var isGroupOnLoadEnabled: Boolean = false,
    )

    private var state = State()
    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }
}
