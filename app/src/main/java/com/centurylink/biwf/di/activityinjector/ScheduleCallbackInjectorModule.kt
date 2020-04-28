package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ScheduleCallbackInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeScheduleCallbackActivityInjector(): ScheduleCallbackActivity
}