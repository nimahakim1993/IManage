package com.nima.app.imanage.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ToolbarConfig(
    val title: String,
    val showBack: Boolean = false,
    val actions: List<ToolbarAction> = emptyList(),
    val showDrawer: Boolean = false,
    val drawerItems: List<DrawerItem> = emptyList()
)

data class ToolbarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)