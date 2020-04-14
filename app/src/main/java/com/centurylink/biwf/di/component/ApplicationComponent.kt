package com.centurylink.biwf.di.component

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.BaseActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.HomeActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.LoginActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NotificationActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NotificationDetailsActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.SupportActivityInjectorModule
import com.centurylink.biwf.di.fragmentinjector.AccountFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.CustomWebFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DashboardFragmentInjectorModule
import com.centurylink.biwf.di.module.AppModule
import com.centurylink.biwf.di.module.DaggerViewModelFactoryModule
import com.centurylink.biwf.di.module.RepositoryModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
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
        (AndroidInjectionModule::class),
        (AppModule::class),
        (DashboardFragmentInjectorModule::class),
        (AccountFragmentInjectorModule::class),
        (CustomWebFragmentInjectorModule::class),
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