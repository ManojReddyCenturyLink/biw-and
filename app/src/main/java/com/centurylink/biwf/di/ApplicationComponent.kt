package com.centurylink.biwf.di

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.BaseActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.MainActivityInjectorModule
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.MainFragmentInjectorModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (AndroidSupportInjectionModule::class),
        (BaseActivityInjectorModule::class),
        (MainActivityInjectorModule::class),
        (BaseFragmentInjectorModule::class),
        (MainFragmentInjectorModule::class)
    ]
)
interface ApplicationComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(app: BIWFApp): Builder

        fun build(): ApplicationComponent
    }

    fun inject(app: BIWFApp)
}