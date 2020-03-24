package com.centurylink.biwf.di.fragmentinjector

import com.centurylink.biwf.ui.main.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentInjectorModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragmentInjector(): MainFragment
}