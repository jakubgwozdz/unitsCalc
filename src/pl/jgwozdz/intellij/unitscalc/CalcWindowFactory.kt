package pl.jgwozdz.intellij.unitscalc

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Condition
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.content.ContentFactory
import java.text.NumberFormat
import java.util.*
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

/**

 */
class CalcWindowFactory : ToolWindowFactory {

    var toolWindow: ToolWindow? = null
    val calcWindow : CalcToolWindow = CalcToolWindow()
    val calculator : Calculator = Calculator(ExpressionParser(), ExpressionFormatter())
    val historyTableModel = object : DefaultTableModel(0, 2) {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
        override fun getColumnClass(columnIndex: Int): Class<*>? = Expression::class.java
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

        val tcr = createCellRenderer()

        calcWindow.historyTable.setDefaultRenderer(Expression::class.java, tcr)
        calcWindow.historyTable.columnModel.columns.toList()[1].maxWidth = 80

        val version = "1.0"
        val format = NumberFormat.getNumberInstance(Locale.ENGLISH)
        val in_mm = format.format(IN_MM.stripTrailingZeros())
        val in_pt = format.format(IN_PT.stripTrailingZeros())
        val in_px = format.format(IN_PX.stripTrailingZeros())

        calcWindow.label.text = "JGwozdz's Units Calculator v$version; 1in = ${in_mm}mm = ${in_pt}pt = ${in_px}px"

        calcWindow.inputField.addActionListener {
            try {
                val input = calculator.analyze(calcWindow.inputField.text)
                val output = calculator.calulate(input, calcWindow.unitsCombo.selectedItem.toString())
                calcWindow.outputField.text = ExpressionFormatter().format(output)
                historyTableModel.insertRow(0, arrayOf(input, output))
                while (historyTableModel.rowCount > 100) historyTableModel.removeRow(100)
                calcWindow.historyTable.changeSelection(0, 1, false, false)
            } catch (e : Exception) {
                JBPopupFactory.getInstance()
                        ?.createHtmlTextBalloonBuilder(e.toString(), MessageType.ERROR , null)
                        ?.setFadeoutTime(6000)
                        ?.createBalloon()
                        ?.show(RelativePoint.getCenterOf(calcWindow.inputField), Balloon.Position.above)

                calcWindow.outputField.text = e.toString()
            }
        }
    }

    private fun createCellRenderer(): TableCellRenderer {
        return object : DefaultTableCellRenderer.UIResource() {
            override fun setValue(value: Any?) {
                if (value is Expression) {
                    text = calculator.prettyPrint(value)
                } else {
                    text = value.toString()
                }
            }
        }
    }
}

class CalcWindowCondition : Condition<Project> {
    override fun value(project: Project?): Boolean {
        if (project == null) return false
        return true
    }

}
