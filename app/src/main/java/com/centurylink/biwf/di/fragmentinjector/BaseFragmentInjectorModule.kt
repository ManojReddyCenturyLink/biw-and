package com.centurylink.biwf.di.fragmentinjector

import com.centurylink.biwf.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BaseFragmentInjectorModule {

    @ContributesAndroidInjector
    abstract fun contributeBaseFragmentInjector(): BaseFragment
}