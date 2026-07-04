package com.nima.app.imanage.data.db.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class AssetIconType(val value: Int, val icon: ImageVector) {
    DOLLAR(0, Icons.Default.AttachMoney),
    GOLD(1, Icons.Default.Star),
    LAND(2, Icons.Default.Home),
    BOURSE(3, Icons.Default.ShowChart),
    CASH(4, Icons.Default.MonetizationOn),
    BANK(5, Icons.Default.AccountBalance),
    DIAMOND(6, Icons.Default.Diamond),
    CURRENCY(7, Icons.Default.CurrencyExchange),
    DEFAULT(8, Icons.Default.Inventory2);

    companion object {
        fun fromValue(value: Int): AssetIconType =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}
