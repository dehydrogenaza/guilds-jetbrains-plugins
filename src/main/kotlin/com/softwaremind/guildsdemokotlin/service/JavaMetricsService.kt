package com.softwaremind.guildsdemokotlin.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod

@Service(Service.Level.PROJECT)
class JavaMetricsService(private val project: Project) {
    fun analyzeFile(psi: PsiJavaFile): JavaMetrics {
        val classNames = psi.classes.map { it.name ?: "<anonymous>" }
        val methodsByClass = psi.classes.associate { clazz -> Pair(
            clazz.name ?: "<anonymous>",
            clazz.methods.map { buildMethodMetrics(it, project) } )
        }

        return JavaMetrics(classNames, methodsByClass)
    }

    private fun buildMethodMetrics(method: PsiMethod, project: Project): MethodMetrics {
        val complexityService = project.service<ComplexityService>()

        val methodName = method.nameIdentifier?.text
        val lineCount = method.text.lines().size
        val codeLineCount = method.text.lines().filter { it.isNotBlank() && !it.trim().startsWith("//") }.size
        val complexity = complexityService.calculateSimCyclomaticComplexity(method)
        val isDeprecated = method.isDeprecated

        return MethodMetrics(
            name = methodName ?: "<unknown>",
            lineCount = lineCount,
            codeLineCount = codeLineCount,
            cyclomaticComplexity = complexity,
            isDeprecated = isDeprecated
        )
    }
}

data class JavaMetrics(
    val classNames: List<String>,
    val methodsByClass: Map<String, List<MethodMetrics>>,
)

data class MethodMetrics(
    val name: String,
    val lineCount: Int,
    val codeLineCount: Int,
    val cyclomaticComplexity: Int,
    val isDeprecated: Boolean,
)