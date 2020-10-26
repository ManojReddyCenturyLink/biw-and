package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.subscription.SubscriptionStatementActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SubscriptionStatementActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeSupportActivityInjector(): SubscriptionStatementActivity
}
