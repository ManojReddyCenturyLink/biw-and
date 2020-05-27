package com.centurylink.biwf

import android.content.Context
import androidx.multidex.BuildConfig
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.utility.InitUtility
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

open class BIWFApp : MultiDexApplication(),HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

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
        MultiDex.install(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}