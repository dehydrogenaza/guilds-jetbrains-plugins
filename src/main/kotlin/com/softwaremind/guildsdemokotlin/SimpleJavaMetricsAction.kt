package com.softwaremind.guildsdemokotlin

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.softwaremind.guildsdemokotlin.service.ComplexityService

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

        val message = buildMessage(psiFile, project)

        Messages.showInfoMessage(project, message, "Java Metrics for ${psiFile.name}")
    }


    // -- Metrics calculation logic --


    private fun buildMessage(psi: PsiJavaFile, project: Project): String {
        val classes = psi.classes
        val methodsByClass = classes.associateWith { clazz -> clazz.methods.map { buildMethodReport(it, project) } }

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

    private fun buildMethodReport(method: PsiMethod, project: Project): String {
        val complexityService = project.service<ComplexityService>()

        val methodName = method.nameIdentifier?.text
        val lineCount = method.text.lines().size
        val codeLineCount = method.text.lines().filter { it.isNotBlank() && !it.trim().startsWith("//") }.size
        val complexity = complexityService.calculateSimCyclomaticComplexity(method)
        val isDeprecated = if (method.isDeprecated) "YES" else "NO"

        return buildString {
            appendLine("  Method: $methodName")
            appendLine("    - Lines of Text: $lineCount")
            appendLine("    - Lines of Code: $codeLineCount")
            appendLine("    - Deprecated: $isDeprecated")
            appendLine("    - Cyclomatic Complexity: $complexity")
        }
    }
}