package com.softwaremind.guildsdemokotlin

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiCatchSection
import com.intellij.psi.PsiConditionalExpression
import com.intellij.psi.PsiDoWhileStatement
import com.intellij.psi.PsiForStatement
import com.intellij.psi.PsiForeachStatement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSwitchLabelStatement
import com.intellij.psi.PsiSwitchLabeledRuleStatement
import com.intellij.psi.PsiWhileStatement

class SimpleJavaMetricsAction : AnAction() {
    // controls when the action is enabled and/or visible
    override fun update(event: AnActionEvent) {
        val currentEditor = event.getData(CommonDataKeys.EDITOR)

        val psiFile = event.getData(CommonDataKeys.PSI_FILE) //Program Structure Interface - abstraction of code structure
        val isJavaFile = psiFile?.language == JavaLanguage.INSTANCE

        event.presentation.isEnabledAndVisible = currentEditor != null && isJavaFile
//        event.presentation.isEnabled = currentEditor != null && isJavaFile
//        event.presentation.isVisible = currentEditor != null && isJavaFile
    }

    // required if update() is overridden
    // the Jetbrains Platform is very strict about which thread does what
    // UI updates must be done on the EDT (Event Dispatch Thread)
    // computations must be done on a BGT (Background Thread)
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile ?: return

        val message = buildMessage(psiFile)

        Messages.showInfoMessage(project, message, "Java Metrics for ${psiFile.name}")
    }


    // -- Metrics calculation logic --


    private fun buildMessage(psi: PsiJavaFile): String {
        val classes = psi.classes
        val methodsByClass = classes.associateWith { clazz -> clazz.methods.map { buildMethodReport(it) } }

        return buildString {
            classes.forEach { clazz ->
                appendLine("Class: ${clazz.qualifiedName}")
                val methodsReport = methodsByClass[clazz] ?: emptyList()
                if (methodsReport.isEmpty()) {
                    appendLine("  No methods found.")
                } else {
                    methodsReport.forEach { methodReport ->
                        append(methodReport)
                    }
                }
            }
        }
    }

    private fun buildMethodReport(method: PsiMethod): String {
        val methodName = method.nameIdentifier?.text
        val lineCount = method.text.lines().size
        val codeLineCount = method.text.lines().filter { it.isNotBlank() && !it.trim().startsWith("//") }.size
        val complexity = calculateSimCyclomaticComplexity(method)
        val isDeprecated = if (method.isDeprecated) "YES" else "NO"

        return buildString {
            appendLine("  Method: $methodName")
            appendLine("    - Lines of Text: $lineCount")
            appendLine("    - Lines of Code: $codeLineCount")
            appendLine("    - Deprecated: $isDeprecated")
            appendLine("    - Cyclomatic Complexity: $complexity")
        }
    }

    private fun calculateSimCyclomaticComplexity(method: PsiMethod): Int {
        var complexity = 1

        method.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitIfStatement(statement: PsiIfStatement) {
                complexity++
                super.visitIfStatement(statement)
            }

            override fun visitForStatement(statement: PsiForStatement) {
                complexity++
                super.visitForStatement(statement)
            }

            override fun visitForeachStatement(statement: PsiForeachStatement) {
                complexity++
                super.visitForeachStatement(statement)
            }

            override fun visitWhileStatement(statement: PsiWhileStatement) {
                complexity++
                super.visitWhileStatement(statement)
            }

            override fun visitDoWhileStatement(statement: PsiDoWhileStatement) {
                complexity++
                super.visitDoWhileStatement(statement)
            }

            override fun visitSwitchLabelStatement(statement: PsiSwitchLabelStatement) {
                // class switch / case syntax
                complexity++
                super.visitSwitchLabelStatement(statement)
            }

            override fun visitSwitchLabeledRuleStatement(statement: PsiSwitchLabeledRuleStatement) {
                // enhanced switch "case ->" syntax
                complexity++
                super.visitSwitchLabeledRuleStatement(statement)
            }

            override fun visitCatchSection(section: PsiCatchSection) {
                complexity++
                super.visitCatchSection(section)
            }

            override fun visitConditionalExpression(expression: PsiConditionalExpression) {
                // the ternary operator: condition ? a : b
                complexity++
                super.visitConditionalExpression(expression)
            }
        })

        return complexity
    }
}