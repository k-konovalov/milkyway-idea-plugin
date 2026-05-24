package com.github.milkyway.idea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service


@Service(Service.Level.APP)
@State(
    name = "MilkyWaySettings",
    storages = [Storage("milkyway-plugin.xml")]
)
class MilkyWaySettings: PersistentStateComponent<MilkyWaySettings.State> {
    companion object {
        const val PARSER_GRADLE = "Gradle"
        const val PARSER_REGEX  = "RegEx"
        const val THEME_BLACK   = "Black"
        const val THEME_WHITE   = "White"
        const val THEME_COLORED = "Colored"
        fun getInstance(): MilkyWaySettings = service()
    }

    data class State(
        var parser: String = PARSER_REGEX,
        var theme: String = THEME_BLACK,
        var isGroupingByRegionEnabled: Boolean = false,
        var isZoomInOutEnabled: Boolean = false,
        var isDevToolsEnabled: Boolean = false,
        var isAnimationEnabled: Boolean = false,
        var isWebGlEnabled: Boolean = true,
    )

    private var state = State()
    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }
}