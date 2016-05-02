package pl.jgwozdz.intellij.unitscalc.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 *
 */
class KotlinAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent?) {

        println("actionPerformed" + event)

    }
}