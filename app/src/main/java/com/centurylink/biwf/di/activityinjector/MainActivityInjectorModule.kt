package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeMainctivityInjector(): MainActivity
}