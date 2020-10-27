package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.changeappointment.AppointmentBookedActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AppointmentBookedActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeAppointmentBookedActivityInjector(): AppointmentBookedActivity
}
