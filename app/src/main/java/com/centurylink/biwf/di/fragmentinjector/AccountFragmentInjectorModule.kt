package com.centurylink.biwf.di.fragmentinjector

import com.centurylink.biwf.screens.home.account.AccountFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AccountFragmentInjectorModule {

    @ContributesAndroidInjector
    abstract fun contributeAccountFragmentInjector(): AccountFragment
}
