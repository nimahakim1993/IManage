package com.nima.app.imanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nima.app.imanage.data.event.ToolbarEvent
import com.nima.app.imanage.data.event.ToolbarEventBus
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.view.BankAccountScreen
import com.nima.app.imanage.presentation.view.CreateBankAccountScreen
import com.nima.app.imanage.presentation.view.FinancialScreen
import com.nima.app.imanage.presentation.view.HomeScreen
import com.nima.app.imanage.presentation.view.MainToolbar
import com.nima.app.imanage.ui.theme.IManageTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IManageTheme {
                AppScaffold()
            }
        }
    }
}

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val toolbarConfig = toolbarForRoute(
        backStackEntry?.destination?.route,
        navController
    )

    Scaffold(
        topBar = {
            MainToolbar(
                config = toolbarConfig,
                onBackClick = { navController.popBackStack() },

                )
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Financial.route) { FinancialScreen() }
            composable(Screen.BankAccount.route) { BankAccountScreen(navController) }
            composable(
                route = Screen.CreateBankAccount.route,
                arguments = listOf(
                    navArgument(name = "cardId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStack ->
                val cardId = backStack.arguments?.getInt("cardId") ?: -1
                CreateBankAccountScreen(navController, cardId)
            }
        }
    }
}

fun toolbarForRoute(route: String?, navController: NavHostController): ToolbarConfig {
    return when (route) {
        Screen.Home.route -> ToolbarConfig(title = "", actions = listOf(
            ToolbarAction(
                icon = Icons.Outlined.Settings,
                contentDescription = "Settings",
                onClick = { /*...*/ }
            )
        ))

        Screen.Financial.route -> ToolbarConfig(title = "دفتر مالی", showBack = true)
        Screen.BankAccount.route -> ToolbarConfig(title = "حساب بانکی",
            showBack = true,
            actions = listOf(
                ToolbarAction(
                    icon = Icons.Default.Visibility,
                    contentDescription = "Visibility",
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            ToolbarEventBus.send(ToolbarEvent.ToggleSensitive)
                        }
                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit",
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            ToolbarEventBus.send(ToolbarEvent.ToggleEdit)
                        }
                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.Add,
                    contentDescription = "Add Account",
                    onClick = {
                        navController.navigate(Screen.CreateBankAccount.route)
                    }
                ),
            ))

        Screen.CreateBankAccount.route -> ToolbarConfig(
            title = "ایجاد حساب بانکی",
            showBack = true,
        )

        else -> ToolbarConfig("")
    }
}