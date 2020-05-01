package com.centurylink.biwf.di.module

import com.centurylink.biwf.di.qualifier.BaseUrl
import com.centurylink.biwf.di.qualifier.BaseUrlType
import com.centurylink.biwf.service.impl.network.FiberServicesFactory
import com.centurylink.biwf.service.impl.network.RetrofitFactory
import com.centurylink.biwf.service.network.TestRestServices
import dagger.Module
import dagger.Provides
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Provides the configuration for the various REST services.
 */
@Module
class RestServiceConfigModule(
    private val baseUrlFiberServices: String
) {
    @Provides
    fun provideConverter(): Converter.Factory {
        return GsonConverterFactory.create()
    }

    @Provides
    fun provideCallAdapter(): CallAdapter.Factory {
        return RxJava3CallAdapterFactory.createAsync()
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.FIBER_SERVICES)
    fun provideRetrofit(factory: RetrofitFactory): Retrofit {
        return factory.create(baseUrlFiberServices)
    }

    @Provides
    fun provideTestRestServices(factory: FiberServicesFactory): TestRestServices {
        return factory.testRestServices
    }
}
