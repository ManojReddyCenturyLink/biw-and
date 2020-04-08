package com.centurylink.biwf.di.module

import com.centurylink.biwf.repos.CurrentAppointmentRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RepositoryModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideCurrentAppointmentRepo(): CurrentAppointmentRepository {
        return CurrentAppointmentRepository()
    }
}