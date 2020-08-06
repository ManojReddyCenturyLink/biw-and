package com.centurylink.biwf.di.component

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.activityinjector.AdditionalInfoActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.AppointmentBookedActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.BaseActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.CancelSubscriptionDetailsInjectorModule
import com.centurylink.biwf.di.activityinjector.CancelSubscriptionInjectorModule
import com.centurylink.biwf.di.activityinjector.ChangeAppointmentActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.EditPaymentDetailsActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.FAQActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.HomeActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.LoginActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NetworkStatusActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NotificationActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.NotificationDetailsActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.PersonalInfoActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.ScheduleCallbackInjectorModule
import com.centurylink.biwf.di.activityinjector.SubscriptionActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.SubscriptionStatementActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.SupportActivityInjectorModule
import com.centurylink.biwf.di.activityinjector.UsageDetailsActivityInjectorModule
import com.centurylink.biwf.di.fragmentinjector.AccountFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.BaseFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.CustomWebFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DashboardFragmentInjectorModule
import com.centurylink.biwf.di.fragmentinjector.DevicesFragmentInjectorModule
import com.centurylink.biwf.di.activityinjector.QRScanInjectorModule
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
