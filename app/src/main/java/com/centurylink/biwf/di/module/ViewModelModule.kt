package com.centurylink.biwf.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.screens.notification.viewmodel.NotificationViewModel
import com.centurylink.biwf.utility.DaggerViewModelFactory

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class DaggerViewModelFactoryModule {
    @Binds
    abstract fun bindDaggerViewModelFactory(viewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory
}
@Module
abstract class ViewModelModule {

    /**
     * Binding NotificationViewModel using this key "NotificationViewModel::class"
     */
    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    abstract fun bindNotificationViewModel(notificationViewModel: NotificationViewModel): ViewModel
}