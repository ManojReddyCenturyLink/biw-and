package com.centurylink.biwf.di.component

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.module.AppModule
import com.centurylink.biwf.di.module.TestAppModule
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (AppModule::class),
        ( AndroidSupportInjectionModule::class)
    ]
)
interface TestApplicationComponent :ApplicationComponent {
    override fun inject(app: BIWFApp)
}