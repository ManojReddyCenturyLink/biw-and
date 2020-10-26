package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UsageDetailsActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeUsageDetailsActivityInjector(): UsageDetailsActivity
}
