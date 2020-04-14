package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.login.LoginActivity
import com.centurylink.biwf.screens.subscription.ManageSubscriptionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ManageSubscriptionActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeManageSubscriptionActivityInjector(): ManageSubscriptionActivity
}