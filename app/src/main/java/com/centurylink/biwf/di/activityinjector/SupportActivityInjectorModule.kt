package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.support.SupportActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SupportActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeSupportActivityInjector(): SupportActivity
}
