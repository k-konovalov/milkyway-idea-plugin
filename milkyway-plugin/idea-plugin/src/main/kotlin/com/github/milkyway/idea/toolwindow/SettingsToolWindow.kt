package com.github.milkyway.idea.toolwindow

import com.github.milkyway.idea.settings.MilkyWaySettings
import com.github.milkyway.idea.settings.MilkyWaySettings.Companion.PARSER_REGEX
import com.github.milkyway.idea.settings.MilkyWaySettings.Companion.PARSER_GRADLE
import com.github.milkyway.idea.settings.MilkyWaySettings.Companion.THEME_BLACK
import com.github.milkyway.idea.settings.MilkyWaySettings.Companion.THEME_WHITE
import com.github.milkyway.idea.settings.MilkyWaySettings.Companion.THEME_COLORED
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import javax.swing.JButton
import javax.swing.JRadioButton
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import java.awt.GridBagLayout
import java.awt.BorderLayout
import java.awt.Insets
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import javax.swing.JPanel
import javax.swing.BorderFactory
import javax.swing.JComboBox


class SettingsToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val milkyWaySettingsToolWindow = MilkyWaySettingsToolWindow()
        val content = ContentFactory.getInstance().createContent(milkyWaySettingsToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MilkyWaySettingsToolWindow {
        private var clicks = 0

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            val form = JPanel(GridBagLayout()).apply {
                border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
            }

            var y = 0
            val settings = MilkyWaySettings.getInstance()

            form.add(SelectRow("Parser", listOf(PARSER_GRADLE, PARSER_REGEX), settings.state.parser) {
                settings.state.parser = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable DevTools", settings.state.isDevToolsEnabled) {
                settings.state.isDevToolsEnabled = it
            }, gbc(y++))
            form.add(SelectRow("Theme [not implemented]", listOf(THEME_BLACK, THEME_WHITE, THEME_COLORED,), settings.state.theme) {
                settings.state.theme = it
            }, gbc(y++))
            form.add(CheckboxRow("Enable grouping by regions [not implemented]", settings.state.isGroupingByRegionEnabled) {
                settings.state.isGroupingByRegionEnabled = it
            }, gbc(y++))

//            form.add(radioRow("Parser", "Gradle", "RegExp"), gbc(y++))
//            form.add(optionRow("Enable grouping by regions", JCheckBox()), gbc(y++))
//            form.add(optionRow("Export to .dot", JCheckBox()), gbc(y++))
//            form.add(optionRow("Find Critical Path", JCheckBox()), gbc(y++))
//            form.add(selectRow("Display Graph", listOf("Tool Window", "Splitter")), gbc(y++))
//            form.add(buttonRow(), gbc(y++))

            add(form, BorderLayout.NORTH)
        }

        private fun optionRow(text: String, checkBox: JCheckBox): JPanel {
            return JPanel(BorderLayout(8, 0)).apply {
                add(JBLabel(text), BorderLayout.WEST)
                add(checkBox, BorderLayout.EAST)
            }
        }

        private fun radioRow(text: String, option1: String, option2: String): JPanel {
            val first = JRadioButton(option1, true)
            val second = JRadioButton(option2)

            ButtonGroup().apply {
                add(first)
                add(second)
            }

            return JPanel(BorderLayout(8, 0)).apply {
                add(JBLabel(text), BorderLayout.WEST)

                val radios = JPanel().apply {
                    add(first)
                    add(second)
                }

                add(radios, BorderLayout.EAST)
            }
        }

        private fun selectRow(text: String, items: List<String>): JPanel {
            return JPanel(BorderLayout(8, 0)).apply {
                add(JBLabel(text), BorderLayout.WEST)
                add(JComboBox(items.toTypedArray()), BorderLayout.EAST)
            }
        }

        private fun buttonRow(): JPanel {
            val label = JBLabel("Likes: 0")

            return JPanel(FlowLayout(FlowLayout.CENTER, 8, 0)).apply {
                add(JButton("Star on GitHub").apply {
                    addActionListener {
                        clicks++
                        label.text = "Likes: $clicks"
                    }
                })
            }
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

        //        private fun buttonRow(): JPanel{
//            val label = JBLabel("Likes: 0")
//
//            return JPanel(BorderLayout(8, 0)).apply {
//                val left = JPanel().apply {
//                    add(JButton("button 1").apply {
//                        addActionListener {
//                            clicks++
//                            label.text = "Likes: $clicks"
//                        }
//                    })
//                }
//
//                val right = JPanel().apply {
//                    add(label)
//                }
//
//                add(left, BorderLayout.WEST)
//                add(right, BorderLayout.EAST)
//            }
//        }
    }
}