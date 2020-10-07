package com.centurylink.biwf.di.component

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.*
import com.centurylink.biwf.di.fragmentinjector.AccountFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.CustomWebFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DashboardFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DevicesFragmentInjectorModule
import com.centurylink.biwf.di.module.AnalyticsModule
import com.centurylink.biwf.di.module.AppModule
import com.centurylink.biwf.di.module.AuthServiceConfigModule
import com.centurylink.biwf.di.module.AuthServiceModule
import com.centurylink.biwf.di.module.DaggerViewModelFactoryModule
import com.centurylink.biwf.di.module.RestServiceConfigModule
import com.centurylink.biwf.di.module.RestServiceModule
import com.centurylink.biwf.di.module.SharedPreferencesModule
import com.centurylink.biwf.di.module.WorkManagerModule
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
        (SubscriptionStatementActivityInjectorModule::class),
        (PersonalInfoActivityInjectorModule::class),
        (CancelSubscriptionInjectorModule::class),
        (ScheduleCallbackInjectorModule::class),
        (AdditionalInfoActivityInjectorModule::class),
        (SelectTimeActivityInjectorModule::class),
        (ContactInfoActivityInjectorModule::class),
        (SubscriptionActivityInjectorModule::class),
        (EditPaymentDetailsActivityInjectorModule::class),
        (NetworkStatusActivityInjectorModule::class),
        (UsageDetailsActivityInjectorModule::class),
        (ChangeAppointmentActivityInjectorModule::class),
        (AppointmentBookedActivityInjectorModule::class),
        (BaseFragmentInjectorModule::class),
        (DashboardFragmentInjectorModule::class),
        (AccountFragmentInjectorModule::class),
        (CustomWebFragmentInjectorModule::class),
        (DaggerViewModelFactoryModule::class),
        (AndroidInjectionModule::class),
        (AppModule::class),
        (CancelSubscriptionDetailsInjectorModule::class),
        (SharedPreferencesModule::class),
        (WorkManagerModule::class),
        (AuthServiceConfigModule::class),
        (AuthServiceModule::class),
        (RestServiceConfigModule::class),
        (RestServiceModule::class),
        (DevicesFragmentInjectorModule::class),
        (QRScanInjectorModule::class),
        (AppAuthResponseServiceInjectorModule::class),
        (AnalyticsModule::class)
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
