package com.github.milkyway.idea.presentation.toolwindow

import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JPanel

class CheckboxRow(
    label: String,
    initialValue: Boolean = false,
    onChange: (Boolean) -> Unit
): JPanel(BorderLayout(8, 0)) {
    private val checkbox = JCheckBox().apply {
        isSelected = initialValue

        addActionListener {
            onChange(isSelected)
        }
    }

    init {
        add(JBLabel(label), BorderLayout.WEST)
        add(checkbox, BorderLayout.EAST)
    }

    var selected: Boolean
        get() = checkbox.isSelected
        set(value) {
            checkbox.isSelected = value
        }
}
