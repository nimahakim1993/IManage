package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.data.repository.BankCardRepository
import com.nima.app.imanage.data.repository.LoanRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { BankCardRepository(get()) }
    single { LoanRepository(get()) }
}