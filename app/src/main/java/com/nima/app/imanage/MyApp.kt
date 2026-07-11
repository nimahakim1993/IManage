package com.nima.app.imanage

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.nima.app.imanage.presentation.di.databaseModule
import com.nima.app.imanage.presentation.di.repositoryModule
import com.nima.app.imanage.presentation.di.utilModule
import com.nima.app.imanage.presentation.di.viewModelModule
import com.nima.app.imanage.receiver.BootReceiver
import com.nima.app.imanage.util.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(
                databaseModule,
                repositoryModule,
                viewModelModule,
                utilModule
            )
        }

        val notifHelper: NotificationHelper = get(NotificationHelper::class.java)
        notifHelper.createChannel()

        BootReceiver.scheduleDailyNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }
        }
    }
}