package com.nima.app.imanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.view.BankCardsScreen
import com.nima.app.imanage.presentation.view.CreateBankCardScreen
import com.nima.app.imanage.presentation.view.FinancialScreen
import com.nima.app.imanage.presentation.view.HomeScreen
import com.nima.app.imanage.presentation.view.MainToolbar
import com.nima.app.imanage.ui.theme.IManageTheme

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

    val toolbarState = remember { mutableStateOf<ToolbarConfig?>(null) }

    Scaffold(
        topBar = {
            toolbarState.value?.let { config ->
                MainToolbar(
                    config = config,
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    ) { padding ->

        Navigation(
            padding = padding,
            navController = navController,
            setToolbar = { toolbarConfig ->
                toolbarState.value = toolbarConfig
            }
        )
    }
}

@Composable
fun Navigation(
    padding: PaddingValues,
    navController: NavHostController,
    setToolbar: (ToolbarConfig) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Home.route) { HomeScreen(setToolbar, navController) }
        composable(Screen.Financial.route) { FinancialScreen(setToolbar) }
        composable(Screen.BankCards.route) { BankCardsScreen(setToolbar, navController) }
        composable(
            route = Screen.CreateBankCard.route,
            arguments = listOf(
                navArgument(name = "cardId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val cardId = backStack.arguments?.getInt("cardId") ?: -1
            CreateBankCardScreen(navController, cardId, setToolbar)
        }
    }
}