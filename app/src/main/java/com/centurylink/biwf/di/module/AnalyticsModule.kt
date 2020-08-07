@file:Suppress("unused")

package com.centurylink.biwf.di.module

import com.centurylink.biwf.BIWFApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class AnalyticsModule {
    @Singleton
    @Provides
    open fun providesAnalytics(biwfApp: BIWFApp): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(biwfApp.applicationContext)
    }
}