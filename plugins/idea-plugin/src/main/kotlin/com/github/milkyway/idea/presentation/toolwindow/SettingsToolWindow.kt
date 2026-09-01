package com.github.milkyway.idea.presentation.toolwindow

import com.github.milkyway.idea.platform.settings.ParserSettings
import com.github.milkyway.idea.platform.settings.ParserSettings.Companion.PARSER_GRADLE
import com.github.milkyway.idea.platform.settings.ParserSettings.Companion.PARSER_REGEX
import com.github.milkyway.idea.platform.settings.VisualizerSettings
import com.github.milkyway.idea.platform.settings.VisualizerSettings.Companion.THEME_BLACK
import com.github.milkyway.idea.platform.settings.VisualizerSettings.Companion.THEME_COLORED
import com.github.milkyway.idea.platform.settings.VisualizerSettings.Companion.THEME_WHITE
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JPanel


class SettingsToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val milkyWaySettingsToolWindow = MilkyWaySettingsToolWindow()
        val content = ContentFactory.getInstance().createContent(milkyWaySettingsToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MilkyWaySettingsToolWindow {
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            val form = JPanel(GridBagLayout()).apply {
                border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
            }

            var y = 0
            val parserSettings = ParserSettings.getInstance()
            val vizSettings = VisualizerSettings.getInstance()

            form.add(SelectRow("Parser", listOf(PARSER_GRADLE, PARSER_REGEX), parserSettings.state.parser) {
                parserSettings.state.parser = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable Rerendering on File Open", parserSettings.state.isRerenderOnFileOpenEnabled) {
                parserSettings.state.isRerenderOnFileOpenEnabled = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable DevTools", vizSettings.state.isDevToolsEnabled) {
                vizSettings.state.isDevToolsEnabled = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable WebGL", vizSettings.state.isWebGlEnabled) {
                vizSettings.state.isWebGlEnabled = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable Animation", vizSettings.state.isAnimationEnabled) {
                vizSettings.state.isAnimationEnabled = it
            }, gbc(y++))
            form.add(SelectRow("Theme [not implemented]", listOf(THEME_BLACK, THEME_WHITE, THEME_COLORED), vizSettings.state.theme) {
                vizSettings.state.theme = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable grouping on load", vizSettings.state.isGroupOnLoadEnabled) {
                vizSettings.state.isGroupOnLoadEnabled = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable grouping by regions [not implemented]", vizSettings.state.isGroupingByRegionEnabled) {
                vizSettings.state.isGroupingByRegionEnabled = it
            }, gbc(y++))

            add(form, BorderLayout.NORTH)
        }

        private fun gbc(y: Int): GridBagConstraints =
            GridBagConstraints().apply {
                gridx = 0
                gridy = y
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.NORTH
                insets = Insets(4, 0, 4, 0)
            }
    }
}
