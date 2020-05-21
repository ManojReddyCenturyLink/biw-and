package com.centurylink.biwf.di.module

import com.centurylink.biwf.service.impl.network.EitherCallAdapterFactory
import com.centurylink.biwf.service.impl.network.EitherConverterFactory
import com.centurylink.biwf.service.impl.network.FiberErrorConverterFactory
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

class TestAppModule : AppModule() {

    @Singleton
    @Provides
    fun provideMockServer(): MockWebServer {
        return MockWebServer()
    }

    @Singleton
    @Provides
    fun provideRetrofitForMock(mockWebServer: MockWebServer): Retrofit {
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(EitherCallAdapterFactory())
            .addConverterFactory(EitherConverterFactory())
            .addConverterFactory(FiberErrorConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}