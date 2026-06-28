package com.nima.app.imanage

import android.content.Context
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
import com.nima.app.imanage.presentation.view.CreateLoanScreen
import com.nima.app.imanage.presentation.view.CreateNoteBoxScreen
import com.nima.app.imanage.presentation.view.CreateNoteScreen
import com.nima.app.imanage.presentation.view.ExpenseCategoriesScreen
import com.nima.app.imanage.presentation.view.ExpensesScreen
import com.nima.app.imanage.presentation.view.LoansScreen
import com.nima.app.imanage.presentation.view.FinancialScreen
import com.nima.app.imanage.presentation.view.HomeScreen
import com.nima.app.imanage.presentation.view.MainToolbar
import com.nima.app.imanage.presentation.view.NoteBoxDetailScreen
import com.nima.app.imanage.presentation.view.NotesScreen
import com.nima.app.imanage.presentation.view.SettingsScreen
import com.nima.app.imanage.ui.theme.IManageTheme
import com.nima.app.imanage.util.LanguageManager
import com.nima.app.imanage.util.ThemeManager

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IManageTheme(themeMode = ThemeManager.getThemeMode(this)) {
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

        composable(Screen.Financial.route) { FinancialScreen(setToolbar, navController) }
        composable(Screen.Loans.route) { LoansScreen(setToolbar, navController) }
        composable(
            route = Screen.CreateLoan.route,
            arguments = listOf(
                navArgument(name = "loanId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val loanId = backStack.arguments?.getInt("loanId") ?: -1
            CreateLoanScreen(setToolbar, navController, loanId)
        }
        composable(Screen.Settings.route) { SettingsScreen(setToolbar, navController) }

        composable(Screen.Notes.route) { NotesScreen(setToolbar, navController) }
        composable(
            route = Screen.CreateNoteBox.route,
            arguments = listOf(
                navArgument(name = "boxId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val boxId = backStack.arguments?.getInt("boxId") ?: -1
            CreateNoteBoxScreen(setToolbar, navController, boxId)
        }
        composable(
            route = Screen.NoteBoxDetail.route,
            arguments = listOf(
                navArgument(name = "boxId") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->
            val boxId = backStack.arguments?.getInt("boxId") ?: -1
            NoteBoxDetailScreen(setToolbar, navController, boxId)
        }
        composable(
            route = Screen.CreateNote.route,
            arguments = listOf(
                navArgument(name = "boxId") {
                    type = NavType.IntType
                },
                navArgument(name = "noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val boxId = backStack.arguments?.getInt("boxId") ?: -1
            val noteId = backStack.arguments?.getInt("noteId") ?: -1
            CreateNoteScreen(setToolbar, navController, boxId, noteId)
        }

        composable(Screen.Expenses.route) { ExpensesScreen(setToolbar, navController) }
        composable(Screen.ExpenseCategories.route) { ExpenseCategoriesScreen(setToolbar, navController) }
    }
}