package com.centurylink.biwf.di.module

import com.centurylink.biwf.di.qualifier.BaseUrl
import com.centurylink.biwf.di.qualifier.BaseUrlType
import com.centurylink.biwf.di.qualifier.ClientType
import com.centurylink.biwf.di.qualifier.HttpClient
import com.centurylink.biwf.network.LiveDataCallAdapterFactory
import com.centurylink.biwf.service.impl.network.asFactory
import com.centurylink.biwf.service.network.ApiServices
import com.centurylink.biwf.service.network.ServicesFactory
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.service.network.create
import dagger.Module
import dagger.Provides
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Provides the configuration for the various REST services.
 *
 * @property baseUrlFiberServices The base-url to the Salesforce Fiber Services server
 * @property baseUrlForAwsBucket The base-url to the AWS Bucket server
 * @property fakeServicesFactory Factory of network-services; if not null, this factory will be used for
 * all network/rest-services.
 */
@Module
class RestServiceConfigModule(
    private val baseUrlFiberServices: String,
    private val baseUrlForAwsBucket: String,
    private val fakeServicesFactory: ServicesFactory? = null
) {
    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.FIBER_SERVICES)
    fun provideRetrofit(@HttpClient(ClientType.OAUTH) client: okhttp3.Call.Factory): ServicesFactory {
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlFiberServices)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createAsync())
            .build()
            .asFactory
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.AWS_BUCKET_SERVICES)
    fun provideRetrofitForAws(@HttpClient(ClientType.NONE) client: okhttp3.Call.Factory): ServicesFactory {
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlForAwsBucket)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .asFactory
    }

    @Singleton
    @Provides
    fun provideTestRestServices(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): TestRestServices {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideApiServices(@BaseUrl(BaseUrlType.AWS_BUCKET_SERVICES) factory: ServicesFactory): ApiServices {
        return factory.create()
    }
}
