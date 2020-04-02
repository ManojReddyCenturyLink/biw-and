package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.home.HomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeActivityInjector(): HomeActivity
}