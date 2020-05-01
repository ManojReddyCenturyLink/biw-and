package com.centurylink.biwf.di.module
import com.centurylink.biwf.service.network.ApiServices
import org.amshove.kluent.mock

class TestAppModule :AppModule(){
    fun provideRetrofitService(): ApiServices = mock()
}