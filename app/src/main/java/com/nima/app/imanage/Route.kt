package com.nima.app.imanage

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Financial : Screen("financial")
    data object Note : Screen("note")
    data object Report : Screen("report")
    data object BankCards : Screen("bankCards")
    data object Loans : Screen("loans")
    data object CreateLoan : Screen("createLoan?loanId={loanId}") {
        fun createRoute(loanId: Int? = null): String {
            return if (loanId == null)
                "createLoan"
            else
                "createLoan?loanId=$loanId"
        }
    }
    data object CreateBankCard : Screen("createBankCard?cardId={cardId}") {
        fun createRoute(cardId: Int? = null): String {
            return if (cardId == null)
                "createBankCard"
            else
                "createBankCard?cardId=$cardId"
        }
    }
    data object SharedTrip : Screen("sharedTrip")
    data object Assets : Screen("assets")
    data object Settings : Screen("settings")
    data object Test : Screen("test/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}