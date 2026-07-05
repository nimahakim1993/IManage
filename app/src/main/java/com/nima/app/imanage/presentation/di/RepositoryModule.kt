package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.data.repository.AssetRepository
import com.nima.app.imanage.data.repository.BankCardRepository
import com.nima.app.imanage.data.repository.ExpenseCategoryRepository
import com.nima.app.imanage.data.repository.ExpenseRepository
import com.nima.app.imanage.data.repository.IncomeRepository
import com.nima.app.imanage.data.repository.IncomeSourceRepository
import com.nima.app.imanage.data.repository.InstallmentItemRepository
import com.nima.app.imanage.data.repository.InstallmentRepository
import com.nima.app.imanage.data.repository.LoanRepository
import com.nima.app.imanage.data.repository.NoteBoxRepository
import com.nima.app.imanage.data.repository.NoteRepository
import com.nima.app.imanage.data.repository.PasswordItemRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { BankCardRepository(get()) }
    single { LoanRepository(get()) }
    single { NoteBoxRepository(get()) }
    single { NoteRepository(get()) }
    single { ExpenseCategoryRepository(get()) }
    single { ExpenseRepository(get()) }
    single { IncomeSourceRepository(get()) }
    single { IncomeRepository(get()) }
    single { InstallmentRepository(get()) }
    single { InstallmentItemRepository(get()) }
    single { AssetRepository(get()) }
    single { PasswordItemRepository(get()) }
}