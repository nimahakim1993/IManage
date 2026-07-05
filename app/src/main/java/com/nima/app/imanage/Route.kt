package com.nima.app.imanage

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Financial : Screen("financial")
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
    data object Passwords : Screen("passwords")
    data object Settings : Screen("settings")

    data object Notes : Screen("notes")
    data object CreateNoteBox : Screen("createNoteBox?boxId={boxId}") {
        fun createRoute(boxId: Int? = null): String =
            if (boxId == null) "createNoteBox" else "createNoteBox?boxId=$boxId"
    }
    data object NoteBoxDetail : Screen("noteBox/{boxId}") {
        fun createRoute(boxId: Int) = "noteBox/$boxId"
    }
    data object CreateNote : Screen("createNote/{boxId}?noteId={noteId}") {
        fun createRoute(boxId: Int, noteId: Int? = null): String =
            if (noteId == null) "createNote/$boxId" else "createNote/$boxId?noteId=$noteId"
    }

    data object Expenses : Screen("expenses")
    data object ExpenseCategories : Screen("expenseCategories")
    data object Incomes : Screen("incomes")
    data object IncomeSources : Screen("incomeSources")
    data object Installments : Screen("installments")
    data object CreateInstallment : Screen("createInstallment?installmentId={installmentId}") {
        fun createRoute(installmentId: Int? = null): String =
            if (installmentId == null) "createInstallment" else "createInstallment?installmentId=$installmentId"
    }
    data object InstallmentDetail : Screen("installmentDetail/{installmentId}") {
        fun createRoute(installmentId: Int) = "installmentDetail/$installmentId"
    }
}