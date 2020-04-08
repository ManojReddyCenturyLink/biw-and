package com.centurylink.biwf.di.module
import com.centurylink.biwf.network.api.ApiServices
import org.amshove.kluent.mock

class TestAppModule :AppModule(){
    override fun provideRetrofitService(): ApiServices = mock()
}