package com.centurylink.biwf.di.fragmentinjector

import com.centurylink.biwf.screens.home.devices.DevicesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DevicesFragmentInjectorModule {

    @ContributesAndroidInjector
    abstract fun contributeDeviceFragmentInjector(): DevicesFragment
}