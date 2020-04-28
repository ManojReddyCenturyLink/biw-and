package com.centurylink.biwf.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.screens.home.account.AccountViewModel
import com.centurylink.biwf.screens.home.dashboard.DashboardViewModel
import com.centurylink.biwf.screens.login.LoginViewModel
import com.centurylink.biwf.screens.notification.NotificationViewModel
import com.centurylink.biwf.screens.subscription.CancelSubscriptionViewModel
import com.centurylink.biwf.screens.support.FAQViewModel
import com.centurylink.biwf.screens.support.SupportViewModel
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
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

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
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancelSubscriptionViewModel::class)
    abstract fun bindCancelSubscriptionViewModel(cancelSubscriptionViewModel: CancelSubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleCallbackViewModel::class)
    abstract fun bindScheduleCallbackViewModel(scheduleCallbackViewModel: ScheduleCallbackViewModel): ViewModel
}