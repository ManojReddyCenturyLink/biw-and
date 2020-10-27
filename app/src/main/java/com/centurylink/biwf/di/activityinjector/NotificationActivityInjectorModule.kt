package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.notification.NotificationActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class NotificationActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeNotificationActivityInjector(): NotificationActivity
}
