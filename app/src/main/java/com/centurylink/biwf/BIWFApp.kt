package com.centurylink.biwf

import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.utility.InitUtility
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

open class BIWFApp : MultiDexApplication(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    lateinit var navigator: Navigator

    override fun onCreate() {
        super.onCreate()
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
    }
}