package com.centurylink.biwf.di.module

import android.content.Context
import android.content.res.Resources
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.network.LiveDataCallAdapterFactory
import com.centurylink.biwf.network.api.ApiServices
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
open class AppModule {

    companion object {
        private const val BASE_URL = "https://bucketforapi.s3-eu-west-1.amazonaws.com/";
    }

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

    /**
     * Provides ApiServices client for Retrofit
     */
    @Singleton
    @Provides
    open fun provideRetrofitService(): ApiServices {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(ApiServices::class.java)
    }

    @Provides
    @Singleton
    open fun giveGSONInstance(): Gson = Gson()
}