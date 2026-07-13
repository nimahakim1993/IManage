package com.nima.app.imanage.util

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen

object SecurityManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_PREFIX = "biometric_module_"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    data class ModuleEntry(
        val key: String,
        val screen: Screen,
        val titleRes: Int,
        val icon: ImageVector,
        val color: Color
    )

    val modules = listOf(
        ModuleEntry("financial", Screen.Financial, R.string.financial_title, Icons.Default.Payments,
            Color(0xFF4CAF50)),
        ModuleEntry("bankCards", Screen.BankCards, R.string.bank_account_home, Icons.Default.AccountBalance,
            Color(0xFF2196F3)),
        ModuleEntry("tripList", Screen.TripList, R.string.shared_trip, Icons.Default.Groups,
            Color(0xFF9C27B0)),
        ModuleEntry("notes", Screen.Notes, R.string.note, Icons.Default.NoteAlt,
            Color(0xFFFF9800)),
        ModuleEntry("assets", Screen.Assets, R.string.assets, Icons.Default.AccountBalanceWallet,
            Color(0xFF009688)),
        ModuleEntry("passwords", Screen.Passwords, R.string.passwords_home, Icons.Default.Password,
            Color(0xFFE91E63)),
        ModuleEntry(
            "carServices", Screen.CarServices, R.string.car_services, Icons.Default.DirectionsCar,
            Color(0xFF795548)
        ),
        ModuleEntry("report", Screen.Report, R.string.report, Icons.Default.Assessment,
            Color(0xFF3F51B5)),
        ModuleEntry("office", Screen.Office, R.string.office_title, Icons.Default.CalendarMonth,
            Color(0xFF607D8B)),
    )

    fun isModuleProtected(context: Context, key: String): Boolean {
        return prefs(context).getBoolean(KEY_PREFIX + key, false)
    }

    fun setModuleProtected(context: Context, key: String, protected: Boolean) {
        prefs(context).edit().putBoolean(KEY_PREFIX + key, protected).apply()
    }

    fun isRouteProtected(context: Context, route: String): Boolean {
        val entry = modules.find { it.screen.route == route } ?: return false
        return isModuleProtected(context, entry.key)
    }
}
