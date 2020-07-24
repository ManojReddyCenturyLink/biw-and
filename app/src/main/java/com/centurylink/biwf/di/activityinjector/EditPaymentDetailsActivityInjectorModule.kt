package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.subscription.EditPaymentDetailsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class EditPaymentDetailsActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeEditPaymentDetailsActivityInjector(): EditPaymentDetailsActivity
}
