package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.presentation.viewmodel.ExpenseCategoryViewModel
import com.nima.app.imanage.presentation.viewmodel.ExpenseViewModel
import com.nima.app.imanage.presentation.viewmodel.IncomeViewModel
import com.nima.app.imanage.presentation.viewmodel.InstallmentViewModel
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.presentation.viewmodel.NoteBoxViewModel
import com.nima.app.imanage.presentation.viewmodel.NoteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { BankCardViewModel(get()) }
    viewModel { LoanViewModel(get()) }
    viewModel { NoteBoxViewModel(get()) }
    viewModel { NoteViewModel(get()) }
    viewModel { ExpenseCategoryViewModel(get()) }
    viewModel { ExpenseViewModel(get(), get()) }
    viewModel { IncomeViewModel(get(), get()) }
    viewModel { InstallmentViewModel(get(), get()) }
}