package com.centurylink.biwf.service.network

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.TroubleshootingModel
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.model.support.FaqModel
import com.centurylink.biwf.network.Resource
import retrofit2.http.GET

interface ApiServices {

    @GET("faqResponse.json")
    fun getFaqDetails(): LiveData<Resource<FaqModel>>

    @GET("troubleshooting.json")
    fun getTroubleshootingDetails(): LiveData<Resource<TroubleshootingModel>>

    @GET("faq.json")
    fun getFAQDetails(): LiveData<Resource<FAQ>>
}