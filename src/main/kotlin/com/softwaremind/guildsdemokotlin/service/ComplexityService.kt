package com.softwaremind.guildsdemokotlin.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiCatchSection
import com.intellij.psi.PsiConditionalExpression
import com.intellij.psi.PsiDoWhileStatement
import com.intellij.psi.PsiForStatement
import com.intellij.psi.PsiForeachStatement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSwitchLabelStatement
import com.intellij.psi.PsiSwitchLabeledRuleStatement
import com.intellij.psi.PsiWhileStatement

@Service(Service.Level.PROJECT)
class ComplexityService(private val project: Project) {
    fun calculateSimCyclomaticComplexity(method: PsiMethod): Int {
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