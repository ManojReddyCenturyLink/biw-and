package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CancelSubscriptionInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeCancelSubscriptionActivityInjector(): CancelSubscriptionActivity
}