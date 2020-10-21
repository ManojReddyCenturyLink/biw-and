package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.support.schedulecallback.ContactInfoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContactInfoActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeContactInfoActivityInjector(): ContactInfoActivity
}