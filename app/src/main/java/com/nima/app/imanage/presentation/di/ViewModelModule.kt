package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { BankCardViewModel(get()) }
}