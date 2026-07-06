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

enum class CarServiceIconType(val value: Int, val icon: ImageVector) {
    OIL_CHANGE(0, Icons.Default.OilBarrel),
    TIRE_CHANGE(1, Icons.Default.TireRepair),
    BRAKE_PAD(2, Icons.Default.CarRepair),
    FILTER(3, Icons.Default.MiscellaneousServices),
    BELT(4, Icons.Default.Settings),
    LAMP(5, Icons.Default.Lightbulb),
    BATTERY(6, Icons.Default.Build),
    ENGINE(7, Icons.Default.Handyman),
    GENERAL(8, Icons.Default.DirectionsCar),
    INSURANCE(9, Icons.Default.Security),
    DEFAULT(10, Icons.Default.CarRepair);

    companion object {
        fun fromValue(value: Int): CarServiceIconType =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}
