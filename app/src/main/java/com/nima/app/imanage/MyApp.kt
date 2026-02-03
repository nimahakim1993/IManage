package com.nima.app.imanage

import android.app.Application
import com.nima.app.imanage.presentation.di.databaseModule
import com.nima.app.imanage.presentation.di.repositoryModule
import com.nima.app.imanage.presentation.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(
                databaseModule,
                repositoryModule,
                viewModelModule
            )
        }
    }
}