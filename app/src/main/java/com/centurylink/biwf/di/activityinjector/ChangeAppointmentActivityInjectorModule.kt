package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.changeappointment.ChangeAppointmentActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChangeAppointmentActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeChangeAppointmentActivityInjector(): ChangeAppointmentActivity
}
