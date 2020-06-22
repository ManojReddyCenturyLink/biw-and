package com.centurylink.biwf.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsViewModel
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionViewModel
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsViewModel
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.screens.home.account.PersonalInfoViewModel
import com.centurylink.biwf.screens.home.dashboard.DashboardViewModel
import com.centurylink.biwf.screens.notification.NotificationViewModel
import com.centurylink.biwf.screens.subscription.SubscriptionStatementViewModel
import com.centurylink.biwf.screens.subscription.SubscriptionViewModel
import com.centurylink.biwf.screens.support.FAQViewModel
import com.centurylink.biwf.screens.support.SupportViewModel
import com.centurylink.biwf.screens.support.schedulecallback.AdditionalInfoViewModel
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackViewModel
import com.centurylink.biwf.utility.DaggerViewModelFactory
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Module
abstract class DaggerViewModelFactoryModule {
    @Binds
    abstract fun bindDaggerViewModelFactory(viewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    abstract fun bindDashboardViewModel(dashboardViewModel: DashboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SupportViewModel::class)
    abstract fun bindSupportViewModel(supportViewModel: SupportViewModel): ViewModel

    /**
     * Binding NotificationViewModel using this key "NotificationViewModel::class"
     */
    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    abstract fun bindNotificationViewModel(notificationViewModel: NotificationViewModel): ViewModel

    /**
     * Binding FAQViewModel using this key "FAQViewModel::class"
     */
    @Binds
    @IntoMap
    @ViewModelKey(FAQViewModel::class)
    abstract fun bindFAQViewModel(faqModel: FAQViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancelSubscriptionViewModel::class)
    abstract fun bindCancelSubscriptionViewModel(cancelSubscriptionViewModel: CancelSubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancelSubscriptionDetailsViewModel::class)
    abstract fun bindCancelSubscriptionDetailsViewModel(cancelSubscriptionDetailsViewModel: CancelSubscriptionDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleCallbackViewModel::class)
    abstract fun bindScheduleCallbackViewModel(scheduleCallbackViewModel: ScheduleCallbackViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AdditionalInfoViewModel::class)
    abstract fun bindAdditionalInfoViewModel(additionalInfoViewModel: AdditionalInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PersonalInfoViewModel::class)
    abstract fun bindPersonalInfoViewModel(personalInfoViewModel: PersonalInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionViewModel::class)
    abstract fun bindSubscriptionViewModel(subscriptionViewModel: SubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionStatementViewModel::class)
    abstract fun bindSubscriptionStatementViewModel(subscriptionStatementViewModel: SubscriptionStatementViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UsageDetailsViewModel::class)
    abstract fun bindUsageDetailsViewModel(usageDetailsViewModel: UsageDetailsViewModel): ViewModel
}