package com.centurylink.biwf

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.service.impl.workmanager.MainWorkerFactory
import com.centurylink.biwf.utility.InitUtility
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

open class BIWFApp : Application(), HasAndroidInjector, Configuration.Provider {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var delegatingWorkerFactory: MainWorkerFactory

    lateinit var navigator: Navigator

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Timber.plant(Timber.DebugTree())
        //init Dagger dependency injection
        InitUtility.initDependencyInjection(this)
        navigator = Navigator()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(delegatingWorkerFactory)
            .build()
}
