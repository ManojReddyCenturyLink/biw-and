package com.centurylink.biwf.di.fragmentinjector

import com.centurylink.biwf.screens.common.CustomWebFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CustomWebFragmentInjectorModule {

    @ContributesAndroidInjector
    abstract fun contributeCustomWebViewFragmentInjector(): CustomWebFragment
}