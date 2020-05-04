package com.centurylink.biwf.di.serviceinjector

import com.centurylink.biwf.service.impl.auth.AppAuthResponseService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AppAuthResponseServiceInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeAppAuthResponseServiceInjector(): AppAuthResponseService
}
