package com.centurylink.biwf.di.module

import android.content.Context
import android.content.res.Resources
import com.centurylink.biwf.BIWFApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
open class AppModule {
    /**
     * Application application level context.
     */
    @Singleton
    @Provides
    open fun provideContext(application: BIWFApp): Context {
        return application.applicationContext
    }

    /**
     * Application resource provider, so that we can get the Drawable, Color, String etc at runtime
     */
    @Provides
    @Singleton
    open fun providesResources(application: BIWFApp): Resources = application.resources
}
