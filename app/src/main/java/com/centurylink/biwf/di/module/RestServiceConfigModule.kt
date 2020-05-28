package com.centurylink.biwf.di.module

import com.centurylink.biwf.di.qualifier.BaseUrl
import com.centurylink.biwf.di.qualifier.BaseUrlType
import com.centurylink.biwf.di.qualifier.ClientType
import com.centurylink.biwf.di.qualifier.HttpClient
import com.centurylink.biwf.network.LiveDataCallAdapterFactory
import com.centurylink.biwf.service.impl.network.*
import com.centurylink.biwf.service.integration.IntegrationServerService
import com.centurylink.biwf.service.network.*
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Call
import retrofit2.Converter
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
    private val integrationServerService: IntegrationServerService,
    private val fakeServicesFactory: ServicesFactory? = null
) {
    private val primitiveTypeConverters = PrimitiveTypeConverterFactory()

    @Singleton
    @Provides
    fun provideIntegrationServerService() = integrationServerService

    @Singleton
    @Provides
    fun provideJsonConverters(): Converter.Factory {
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(primitiveTypeConverters)
            .create()
        return GsonConverterFactory.create(gson)
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.FIBER_SERVICES)
    fun provideRetrofit(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.OAUTH) client: Call.Factory
    ): ServicesFactory {
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlFiberServices)
            .addCallAdapterFactory(EitherCallAdapterFactory())
            .addConverterFactory(EitherConverterFactory())
            .addConverterFactory(FiberErrorConverterFactory())
            .addConverterFactory(jsonConverters)
            .addConverterFactory(primitiveTypeConverters)
            .build()
            .asFactory
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.AWS_BUCKET_SERVICES)
    fun provideRetrofitForAws(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.NONE) client: Call.Factory
    ): ServicesFactory {
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlForAwsBucket)
            .addConverterFactory(jsonConverters)
            .addConverterFactory(primitiveTypeConverters)
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .asFactory
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.LOCAL_INTEGRATION)
    fun provideRetrofitForMock(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.OAUTH) client: Call.Factory
    ): ServicesFactory {
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(integrationServerService.baseUrl)
            .addConverterFactory(jsonConverters)
            .addConverterFactory(primitiveTypeConverters)
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

    @Singleton
    @Provides
    fun provideAccountServices(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): AccountApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideContactServices(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): ContactApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideUserServices(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): UserService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideZuoraPaymentService(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): ZuoraPaymentService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideAppointmentService(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): AppointmentService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideBillingApiServices(@BaseUrl(BaseUrlType.AWS_BUCKET_SERVICES) factory: ServicesFactory): BillingApiServices {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideZuoraSubscriptionService(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): ZuoraSubscriptionApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideCaseApiServices(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): CaseApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideIntegrationRestServices(@BaseUrl(BaseUrlType.LOCAL_INTEGRATION) factory: ServicesFactory): IntegrationRestServices {
        return factory.create()
    }
}
