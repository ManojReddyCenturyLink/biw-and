package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.support.schedulecallback.AdditionalInfoActivity
import com.centurylink.biwf.screens.support.schedulecallback.SelectTimeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SelectTimeActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeAdditionalInfoActivityInjector(): SelectTimeActivity
}