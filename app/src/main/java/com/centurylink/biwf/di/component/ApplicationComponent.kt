package com.centurylink.biwf.di

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.BaseActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.HomeActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.LoginActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.SupportActivityInjectorModule
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DashboardFragmentInjectorModule
import com.centurylink.biwf.di.module.RepositoryModule
import com.centurylink.biwf.di.module.DaggerViewModelFactoryModule
import com.centurylink.biwf.di.module.ViewModelModule
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
        (DashboardFragmentInjectorModule::class),
        (ViewModelModule::class),
        (RepositoryModule::class)
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