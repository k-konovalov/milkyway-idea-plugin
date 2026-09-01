package com.github.milkyway.idea.toolwindow

import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import javax.swing.JComboBox
import javax.swing.JPanel

class SelectRow(
    label: String,
    options: List<String>,
    selectedValue: String,
    onChange: (String) -> Unit
): JPanel(BorderLayout(8, 0)) {

    private val combobox = JComboBox(options.toTypedArray()).apply {
        selectedItem = selectedValue
        addActionListener {
            selectedItem?.toString()?.let(onChange)
        }
    }

    init {
        add(JBLabel(label), BorderLayout.WEST)
        add(combobox, BorderLayout.EAST)
    }

    var selected: String
        get() = combobox.selectedItem as String
        set(value) {
            combobox.selectedItem = value
        }
}