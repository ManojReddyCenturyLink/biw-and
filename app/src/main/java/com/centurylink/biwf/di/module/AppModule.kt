package com.centurylink.biwf.di.module

import android.content.Context
import android.content.res.Resources
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.network.LiveDataCallAdapterFactory
import com.centurylink.biwf.network.api.ApiServices
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    companion object {
        private const val BASE_URL = "https://api.myjson.com/";
    }

    /**
     * Application application level context.
     */
    @Singleton
    @Provides
    fun provideContext(application: BIWFApp): Context {
        return application.applicationContext
    }

    /**
     * Application resource provider, so that we can get the Drawable, Color, String etc at runtime
     */
    @Provides
    @Singleton
    fun providesResources(application: BIWFApp): Resources = application.resources

    /**
     * Provides ApiServices client for Retrofit
     */
    @Singleton
    @Provides
    fun provideRetrofitService(): ApiServices {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(ApiServices::class.java)
    }
}