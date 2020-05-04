package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.subscription.CancelSubscriptionDetailsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CancelSubscriptionDetailsInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeCancelSubscriptionDetailsActivityInjector(): CancelSubscriptionDetailsActivity
}