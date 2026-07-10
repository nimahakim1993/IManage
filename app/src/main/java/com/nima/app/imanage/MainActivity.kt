package com.nima.app.imanage

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.view.AssetsScreen
import com.nima.app.imanage.presentation.view.BankCardsScreen
import com.nima.app.imanage.presentation.view.CarServicesScreen
import com.nima.app.imanage.presentation.view.CreateBankCardScreen
import com.nima.app.imanage.presentation.view.CreateCarServiceScreen
import com.nima.app.imanage.presentation.view.CreateInstallmentScreen
import com.nima.app.imanage.presentation.view.CreateLoanScreen
import com.nima.app.imanage.presentation.view.CreateNoteBoxScreen
import com.nima.app.imanage.presentation.view.CreateNoteScreen
import com.nima.app.imanage.presentation.view.ExpenseCategoriesScreen
import com.nima.app.imanage.presentation.view.ExpensesScreen
import com.nima.app.imanage.presentation.view.FinancialScreen
import com.nima.app.imanage.presentation.view.HomeScreen
import com.nima.app.imanage.presentation.view.IncomeSourcesScreen
import com.nima.app.imanage.presentation.view.IncomesScreen
import com.nima.app.imanage.presentation.view.InstallmentDetailScreen
import com.nima.app.imanage.presentation.view.InstallmentsScreen
import com.nima.app.imanage.presentation.view.LoansScreen
import com.nima.app.imanage.presentation.view.MainToolbar
import com.nima.app.imanage.presentation.view.NoteBoxDetailScreen
import com.nima.app.imanage.presentation.view.NotesScreen
import com.nima.app.imanage.presentation.view.PasswordItemsScreen
import com.nima.app.imanage.presentation.view.ReportScreen
import com.nima.app.imanage.presentation.view.SettingsScreen
import com.nima.app.imanage.presentation.view.tripsplit.CreateTripScreen
import com.nima.app.imanage.presentation.view.tripsplit.TripDetailScreen
import com.nima.app.imanage.presentation.view.tripsplit.TripExpenseFormScreen
import com.nima.app.imanage.presentation.view.tripsplit.TripListScreen
import com.nima.app.imanage.presentation.view.tripsplit.TripSettlementScreen
import com.nima.app.imanage.ui.theme.IManageTheme
import com.nima.app.imanage.util.LanguageManager
import com.nima.app.imanage.util.ThemeManager

class MainActivity : FragmentActivity() {
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
        composable(Screen.Report.route) { ReportScreen(setToolbar, navController) }
        composable(Screen.Assets.route) { AssetsScreen(setToolbar, navController) }
        composable(Screen.Passwords.route) { PasswordItemsScreen(setToolbar, navController) }
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
        composable(Screen.Incomes.route) { IncomesScreen(setToolbar, navController) }
        composable(Screen.IncomeSources.route) { IncomeSourcesScreen(setToolbar, navController) }
        composable(Screen.Installments.route) { InstallmentsScreen(setToolbar, navController) }
        composable(
            route = Screen.CreateInstallment.route,
            arguments = listOf(
                navArgument(name = "installmentId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val installmentId = backStack.arguments?.getInt("installmentId") ?: -1
            CreateInstallmentScreen(setToolbar, navController, installmentId)
        }
        composable(
            route = Screen.InstallmentDetail.route,
            arguments = listOf(
                navArgument(name = "installmentId") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->
            val installmentId = backStack.arguments?.getInt("installmentId") ?: -1
            InstallmentDetailScreen(setToolbar, navController, installmentId)
        }

        composable(Screen.TripList.route) { TripListScreen(setToolbar, navController) }
        composable(
            route = Screen.CreateTrip.route,
            arguments = listOf(
                navArgument(name = "tripId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val tripId = backStack.arguments?.getInt("tripId") ?: -1
            CreateTripScreen(setToolbar, navController, if (tripId == -1) null else tripId)
        }
        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(
                navArgument(name = "tripId") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->
            val tripId = backStack.arguments?.getInt("tripId") ?: -1
            TripDetailScreen(setToolbar, navController, tripId)
        }
        composable(
            route = Screen.CreateTripExpense.route,
            arguments = listOf(
                navArgument(name = "tripId") {
                    type = NavType.IntType
                },
                navArgument(name = "expenseId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val tripId = backStack.arguments?.getInt("tripId") ?: -1
            val expenseId = backStack.arguments?.getInt("expenseId") ?: -1
            TripExpenseFormScreen(
                setToolbar,
                navController,
                tripId,
                if (expenseId == -1) null else expenseId
            )
        }
        composable(
            route = Screen.TripSettlement.route,
            arguments = listOf(
                navArgument(name = "tripId") {
                    type = NavType.IntType
                }
            )
        ) { backStack ->
            val tripId = backStack.arguments?.getInt("tripId") ?: -1
            TripSettlementScreen(setToolbar, navController, tripId)
        }
        composable(Screen.CarServices.route) { CarServicesScreen(setToolbar, navController) }
        composable(
            route = Screen.CreateCarService.route,
            arguments = listOf(
                navArgument(name = "serviceId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val serviceId = backStack.arguments?.getInt("serviceId") ?: -1
            CreateCarServiceScreen(setToolbar, navController, serviceId)
        }
    }
}