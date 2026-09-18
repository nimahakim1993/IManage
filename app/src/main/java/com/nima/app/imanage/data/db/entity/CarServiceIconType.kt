package com.nima.app.imanage.data.db.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.ui.graphics.vector.ImageVector

object CarServiceIcons {
    val icons: List<ImageVector> = listOf(
        Icons.Default.OilBarrel,
        Icons.Default.TireRepair,
        Icons.Default.CarRepair,
        Icons.Default.MiscellaneousServices,
        Icons.Default.Settings,
        Icons.Default.Lightbulb,
        Icons.Default.Build,
        Icons.Default.Handyman,
        Icons.Default.DirectionsCar,
        Icons.Default.Security
    )

    fun fromIndex(index: Int): ImageVector =
        icons.getOrElse(index) { icons.last() }
}
