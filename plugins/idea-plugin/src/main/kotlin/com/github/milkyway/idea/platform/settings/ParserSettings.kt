package com.github.milkyway.idea.platform.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(
    name = "MilkyWayParserSettings",
    storages = [Storage("milkyway-parser.xml")]
)
class ParserSettings : PersistentStateComponent<ParserSettings.State> {
    companion object {
        const val PARSER_GRADLE = "Gradle"
        const val PARSER_REGEX = "RegEx"
        fun getInstance(): ParserSettings = service()
    }

    data class State(
        var parser: String = PARSER_REGEX,
        var isRerenderOnFileOpenEnabled: Boolean = true,
    )

    private var state = State()
    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }
}
