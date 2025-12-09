package com.softwaremind.guildsdemokotlin

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.messages.MessageBusConnection
import com.softwaremind.guildsdemokotlin.service.JavaMetricsService
import javax.swing.JComponent
import javax.swing.JLabel

class SimpleJavaMetricsPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, false), Disposable {
    private lateinit var fileNameLabel: JLabel
    private lateinit var statsGroup: Row
    private lateinit var avgLineCountLabel: JLabel
    private lateinit var avgCodeLineCountLabel: JLabel
    private lateinit var avgComplexityLabel: JLabel
    private lateinit var maxLineCountLabel: JLabel
    private lateinit var maxCodeLineCountLabel: JLabel
    private lateinit var maxComplexityLabel: JLabel

    private val editorConnection: MessageBusConnection = project.messageBus.connect(this)

    init {
        setContent(createUI())

        editorConnection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    updateMetrics()
                }

                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    updateMetrics()
                }

                override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                    updateMetrics()
                }
            }
        )

        updateMetrics()
    }

    private fun createUI(): JComponent =
        panel {
            row {
                fileNameLabel = label("").component
            }

            statsGroup = group("Stats") {
                row("Average") {
                    avgLineCountLabel = label("").component
                    avgCodeLineCountLabel = label("").component
                    avgComplexityLabel = label("").component
                }
                row("Max") {
                    maxLineCountLabel = label("").component
                    maxCodeLineCountLabel = label("").component
                    maxComplexityLabel = label("").component
                }
            }
        }

    private fun updateMetrics() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        val virtualFile = editor?.virtualFile
        if (virtualFile == null) {
            clearMetrics()
            return
        }

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        val javaFile = psiFile as? PsiJavaFile
        if (javaFile == null) {
            clearMetrics()
            return
        }

        val metricsService = project.service<JavaMetricsService>()
        val metrics = metricsService.analyzeFile(javaFile)

        fileNameLabel.text = "File: ${virtualFile.name}"

        val allMethods = metrics.methodsByClass.values.flatten()
        val avgLineCount = allMethods.map { it.lineCount }.average().toInt()
        val avgCodeLineCount = allMethods.map { it.codeLineCount }.average().toInt()
        val avgComplexity = allMethods.map { it.cyclomaticComplexity }.average().toInt()
        val maxLineCount = allMethods.maxOf { it.lineCount }
        val maxCodeLineCount = allMethods.maxOf { it.codeLineCount }
        val maxComplexity = allMethods.maxOf { it.cyclomaticComplexity }

        avgLineCountLabel.text = "Lines: $avgLineCount"
        avgCodeLineCountLabel.text = "Code Lines: $avgCodeLineCount"
        avgComplexityLabel.text = "Complexity: $avgComplexity"
        maxLineCountLabel.text = "Lines: $maxLineCount"
        maxCodeLineCountLabel.text = "Code Lines: $maxCodeLineCount"
        maxComplexityLabel.text = "Complexity: $maxComplexity"

        statsGroup.visible(true)
    }

    private fun clearMetrics() {
        fileNameLabel.text = "Select a Java source to see stats."
        statsGroup.visible(false)
    }

    override fun dispose() {
        //
    }

}