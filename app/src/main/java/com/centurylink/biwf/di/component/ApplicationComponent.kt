package com.centurylink.biwf.di.component

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.BaseActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.CancelSubscriptionDetailsInjectorModule
import com.centurylink.biwf.di.activityinjector.CancelSubscriptionInjectorModule
import com.centurylink.biwf.di.activityinjector.FAQActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.HomeActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.LoginActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NotificationActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NotificationDetailsActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.ScheduleCallbackInjectorModule
import com.centurylink.biwf.di.activityinjector.SupportActivityInjectorModule
import com.centurylink.biwf.di.fragmentinjector.AccountFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.CustomWebFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DashboardFragmentInjectorModule
import com.centurylink.biwf.di.module.AppModule
import com.centurylink.biwf.di.module.AuthServiceConfigModule
import com.centurylink.biwf.di.module.AuthServiceModule
import com.centurylink.biwf.di.module.DaggerViewModelFactoryModule
import com.centurylink.biwf.di.module.RepositoryModule
import com.centurylink.biwf.di.module.RestServiceConfigModule
import com.centurylink.biwf.di.module.RestServiceModule
import com.centurylink.biwf.di.module.SharedPreferencesModule
import com.centurylink.biwf.di.serviceinjector.AppAuthResponseServiceInjectorModule
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
        (FAQActivityInjectorModule::class),
        (NotificationActivityInjectorModule::class),
        (NotificationDetailsActivityInjectorModule::class),
        (CancelSubscriptionInjectorModule::class),
        (ScheduleCallbackInjectorModule::class),
        (AdditionalInfoActivityInjectorModule::class),
        (BaseFragmentInjectorModule::class),
        (DashboardFragmentInjectorModule::class),
        (AccountFragmentInjectorModule::class),
        (CustomWebFragmentInjectorModule::class),
        (DaggerViewModelFactoryModule::class),
        (AndroidInjectionModule::class),
        (AppModule::class),
        (RepositoryModule::class),
        (CancelSubscriptionDetailsInjectorModule::class),
        (SharedPreferencesModule::class),
        (AuthServiceConfigModule::class),
        (AuthServiceModule::class),
        (RestServiceConfigModule::class),
        (RestServiceModule::class),
        (RepositoryModule::class),
        (AppAuthResponseServiceInjectorModule::class)
    ]
)
interface ApplicationComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(app: BIWFApp): Builder

        fun authServiceConfig(authServiceConfigModule: AuthServiceConfigModule): Builder

        fun restServiceConfig(restServiceConfigModule: RestServiceConfigModule): Builder

        fun build(): ApplicationComponent
    }

    fun inject(app: BIWFApp)
}
