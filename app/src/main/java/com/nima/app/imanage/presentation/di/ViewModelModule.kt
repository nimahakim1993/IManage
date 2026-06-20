package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { BankCardViewModel(get()) }
    viewModel { LoanViewModel(get()) }
}