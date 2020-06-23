package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.login.LoginActivity
import com.centurylink.biwf.screens.networkstatus.NetworkStatusActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class NetworkStatusActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeNetworkStatusActivityInjector(): NetworkStatusActivity
}