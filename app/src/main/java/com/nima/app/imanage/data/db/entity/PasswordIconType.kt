package com.nima.app.imanage.data.db.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class PasswordIconType(val value: Int, val icon: ImageVector) {
    EMAIL(0, Icons.Default.Email),
    BANK(1, Icons.Default.AccountBalance),
    WALLET(2, Icons.Default.AccountBalanceWallet),
    SHOPPING(3, Icons.Default.ShoppingCart),
    DEVELOPER(4, Icons.Default.DeveloperMode),
    WORK(5, Icons.Default.Work),
    GAME(6, Icons.Default.SportsEsports),
    LOCK(7, Icons.Default.Lock),
    DEFAULT(8, Icons.Default.VpnKey);

    companion object {
        fun fromValue(value: Int): PasswordIconType =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}