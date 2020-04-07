package com.centurylink.biwf.di.fragmentinjector

import com.centurylink.biwf.screens.dashboard.DashboardFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DashboardFragmentInjectorModule {

    @ContributesAndroidInjector
    abstract fun contributeDashboardFragmentInjector(): DashboardFragment
}