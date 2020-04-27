package com.centurylink.biwf.di.module

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.utility.preferences.Preferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object SharedPreferencesModule {

    @Provides
    @Singleton
    @JvmStatic
    fun providesSharedPrefs(app:BIWFApp) : Preferences {
        return Preferences(app)
    }
}