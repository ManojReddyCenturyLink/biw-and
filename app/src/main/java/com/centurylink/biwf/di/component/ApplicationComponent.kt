package com.centurylink.biwf.di.component

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.*
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.module.AppModule
import com.centurylink.biwf.di.module.DaggerViewModelFactoryModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (AndroidSupportInjectionModule::class),
        (BaseActivityInjectorModule::class),
        (LoginActivityInjectorModule::class),
        (HomeActivityInjectorModule::class),
        (SupportActivityInjectorModule::class),
        (BaseFragmentInjectorModule::class),
        (DaggerViewModelFactoryModule::class),
        (NotificationActivityInjectorModule::class),
        (NotificationDetailsActivityInjectorModule::class),
        (AppModule::class)
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