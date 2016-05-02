package pl.jgwozdz.intellij.unitscalc

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

/**

 */
class CalcWindowFactory : ToolWindowFactory {

    var toolWindow: ToolWindow? = null
    val calcWindow : CalcToolWindow = CalcToolWindow()
    val calculator : Calculator = Calculator()
    val historyTableModel = object : DefaultTableModel(0, 2) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.toolWindow = toolWindow
        resetUI()
        val content = ContentFactory.SERVICE.getInstance().createContent(calcWindow.calcWindowPanel, "", false)

        toolWindow.contentManager.addContent(content)
    }

    private fun resetUI() {
        calcWindow.historyTable.model = historyTableModel
        calcWindow.historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        calcWindow.historyTable.cellSelectionEnabled = true

        calcWindow.inputField.addActionListener {
            val input = calculator.analyze(calcWindow.inputField.text)
            val output = calculator.calulate(input, calcWindow.unitsCombo.selectedItem.toString())
            calcWindow.outputField.text = output
            historyTableModel.insertRow(0, arrayOf(input, output))
        }
    }
}

class CalcWindowCondition : Condition<Project> {
    override fun value(project: Project?): Boolean {
        if (project == null) return false
        return true
    }

}
