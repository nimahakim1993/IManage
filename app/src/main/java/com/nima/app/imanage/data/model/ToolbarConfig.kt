package com.nima.app.imanage.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ToolbarConfig(
    val title: String,
    val showBack: Boolean = false,
    val actions: List<ToolbarAction> = emptyList()
)

data class ToolbarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)