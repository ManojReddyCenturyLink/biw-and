package com.centurylink.biwf.di.module

import androidx.work.WorkManager
import com.centurylink.biwf.BIWFApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(app: BIWFApp): WorkManager {
        return WorkManager.getInstance(app)
    }
}
