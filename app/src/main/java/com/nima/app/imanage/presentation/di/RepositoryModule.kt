package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.data.repository.BankCardRepository
import com.nima.app.imanage.data.repository.LoanRepository
import com.nima.app.imanage.data.repository.NoteBoxRepository
import com.nima.app.imanage.data.repository.NoteRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { BankCardRepository(get()) }
    single { LoanRepository(get()) }
    single { NoteBoxRepository(get()) }
    single { NoteRepository(get()) }
}