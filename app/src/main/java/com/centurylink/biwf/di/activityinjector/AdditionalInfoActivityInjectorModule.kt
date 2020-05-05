package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.support.schedulecallback.AdditionalInfoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AdditionalInfoActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeAdditionalInfoActivityInjector(): AdditionalInfoActivity
}