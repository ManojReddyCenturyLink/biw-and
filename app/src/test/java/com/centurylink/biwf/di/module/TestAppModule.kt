package com.centurylink.biwf.di.module
import android.content.Context
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.network.api.ApiServices
import dagger.Module
import io.mockk.mockk
import org.amshove.kluent.mock

class TestAppModule :AppModule(){
    override fun provideRetrofitService(): ApiServices = mock()

}