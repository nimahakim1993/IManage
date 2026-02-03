package com.nima.app.imanage

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Financial : Screen("financial")
    data object Note : Screen("note")
    data object Report : Screen("report")
    data object BankAccount : Screen("bankAccount")
    data object CreateBankAccount : Screen("createBankAccount?cardId={cardId}") {
        fun createRoute(cardId: Int? = null): String {
            return if (cardId == null)
                "createBankAccount"
            else
                "createBankAccount?cardId=$cardId"
        }
    }
    data object SharedTrip : Screen("sharedTrip")
    data object Assets : Screen("assets")
    data object Test : Screen("test/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}