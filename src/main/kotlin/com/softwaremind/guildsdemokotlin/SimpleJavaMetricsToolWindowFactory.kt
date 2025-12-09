package com.softwaremind.guildsdemokotlin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class SimpleJavaMetricsToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = SimpleJavaMetricsPanel(project)

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, null, false)

        content.setDisposer(panel)

        toolWindow.contentManager.addContent(content)
    }
}