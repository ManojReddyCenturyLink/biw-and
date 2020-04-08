package com.centurylink.biwf.di.activityinjector


import com.centurylink.biwf.screens.support.FAQActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FAQActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeNFAQActivityInjector(): FAQActivity
}