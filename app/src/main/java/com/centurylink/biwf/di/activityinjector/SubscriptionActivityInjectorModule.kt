package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.home.account.subscription.SubscriptionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SubscriptionActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeSubscriptionActivityInjector(): SubscriptionActivity
}