package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class NotificationDetailsActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeNotificationDetailsInjector(): NotificationDetailsActivity
}
