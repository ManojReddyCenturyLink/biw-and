package com.centurylink.biwf.di.module

import com.centurylink.biwf.di.qualifier.BaseUrl
import com.centurylink.biwf.di.qualifier.BaseUrlType
import com.centurylink.biwf.di.qualifier.ClientType
import com.centurylink.biwf.di.qualifier.HttpClient
import com.centurylink.biwf.service.impl.network.AssiaErrorConverterFactory
import com.centurylink.biwf.service.impl.network.EitherCallAdapterFactory
import com.centurylink.biwf.service.impl.network.EitherConverterFactory
import com.centurylink.biwf.service.impl.network.FiberErrorConverterFactory
import com.centurylink.biwf.service.impl.network.McafeeErrorConverterFactory
import com.centurylink.biwf.service.impl.network.PrimitiveTypeConverterFactory
import com.centurylink.biwf.service.impl.network.asFactory
import com.centurylink.biwf.service.integration.IntegrationServerService
import com.centurylink.biwf.service.network.SupportService
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.service.network.AppointmentService
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.service.network.AssiaTokenService
import com.centurylink.biwf.service.network.AssiaTrafficUsageService
import com.centurylink.biwf.service.network.BillingApiServices
import com.centurylink.biwf.service.network.CaseApiService
import com.centurylink.biwf.service.network.ContactApiService
import com.centurylink.biwf.service.network.FaqApiService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.service.network.McafeeApiService
import com.centurylink.biwf.service.network.NotificationService
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.service.network.ServicesFactory
import com.centurylink.biwf.service.network.SpeedTestService
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.service.network.UserService
import com.centurylink.biwf.service.network.WifiNetworkApiService
import com.centurylink.biwf.service.network.WifiStatusService
import com.centurylink.biwf.service.network.ZuoraPaymentService
import com.centurylink.biwf.service.network.ZuoraSubscriptionApiService
import com.centurylink.biwf.service.network.create
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
    private val baseUrlSupportServices: String,
    private val baseUrlForAwsBucket: String,
    // TODO - remove this when all Cloudcheck endpoints are accessed via Apigee
    private val baseUrlForAssiaServices: String,
    private val baseUrlForMcafeeServices: String,
    private val baseUrlForOauthAssiaServices: String,
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
    @BaseUrl(BaseUrlType.SUPPORT_SERVICES)
    fun provideSupportRetrofit(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.OAUTH) client: Call.Factory
    ): ServicesFactory {
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlSupportServices)
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
            .build()
            .asFactory
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.ASSIA_SERVICES)
    fun provideRetrofitForAssia(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.NONE) client: Call.Factory
    ):ServicesFactory{
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlForAssiaServices)
            .addCallAdapterFactory(EitherCallAdapterFactory())
            .addConverterFactory(EitherConverterFactory())
            .addConverterFactory(AssiaErrorConverterFactory())
            .addConverterFactory(jsonConverters)
            .addConverterFactory(primitiveTypeConverters)
            .build()
            .asFactory
    }

    @Singleton
    @Provides
    @BaseUrl(BaseUrlType.ASSIA_OAUTH_SERVICES)
    fun provideRetrofitForOAuthAssia(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.OAUTH) client: Call.Factory
    ):ServicesFactory{
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlForOauthAssiaServices)
            .addCallAdapterFactory(EitherCallAdapterFactory())
            .addConverterFactory(EitherConverterFactory())
            .addConverterFactory(AssiaErrorConverterFactory())
            .addConverterFactory(jsonConverters)
            .addConverterFactory(primitiveTypeConverters)
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
    @BaseUrl(BaseUrlType.MCAFEE_SERVICES)
    fun provideRetrofitForMcafee(
        jsonConverters: Converter.Factory,
        @HttpClient(ClientType.OAUTH) client: Call.Factory
    ):ServicesFactory{
        return fakeServicesFactory ?: Retrofit.Builder()
            .callFactory(client)
            .baseUrl(baseUrlForMcafeeServices)
            .addCallAdapterFactory(EitherCallAdapterFactory())
            .addConverterFactory(EitherConverterFactory())
            .addConverterFactory(McafeeErrorConverterFactory())
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

    @Singleton
    @Provides
    fun provideNotificationApiServices(@BaseUrl(BaseUrlType.LOCAL_INTEGRATION) factory: ServicesFactory): NotificationService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideFaqApiServices(@BaseUrl(BaseUrlType.FIBER_SERVICES) factory: ServicesFactory): FaqApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideAssiaTokenServices(@BaseUrl(BaseUrlType.ASSIA_SERVICES) factory: ServicesFactory): AssiaTokenService{
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideAssiaServices(@BaseUrl(BaseUrlType.ASSIA_SERVICES) factory: ServicesFactory): AssiaService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun providesOauthAsiaService(@BaseUrl(BaseUrlType.ASSIA_OAUTH_SERVICES) factory: ServicesFactory): OAuthAssiaService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun providesAssiaTrafficUsageService(@BaseUrl(BaseUrlType.ASSIA_OAUTH_SERVICES) factory: ServicesFactory): AssiaTrafficUsageService{
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideNetworkManagementAPIServices(@BaseUrl(BaseUrlType.ASSIA_SERVICES) factory: ServicesFactory): WifiNetworkApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun providesMcafeeUsersService(@BaseUrl(BaseUrlType.MCAFEE_SERVICES) factory: ServicesFactory): McafeeApiService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun provideSupportService(@BaseUrl(BaseUrlType.SUPPORT_SERVICES) factory: ServicesFactory): SupportService {
        return factory.create()
    }


    @Singleton
    @Provides
    fun providesWifiStatusService(@BaseUrl(BaseUrlType.ASSIA_OAUTH_SERVICES) factory: ServicesFactory): WifiStatusService {
        return factory.create()
    }

    @Singleton
    @Provides
    fun providesSpeedTestService(@BaseUrl(BaseUrlType.ASSIA_OAUTH_SERVICES) factory: ServicesFactory): SpeedTestService {
        return factory.create()
    }
}
