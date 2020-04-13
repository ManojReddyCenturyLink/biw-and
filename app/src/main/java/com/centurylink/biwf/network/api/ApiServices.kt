package com.centurylink.biwf.network.api

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.support.FaqModel
import com.centurylink.biwf.model.TroubleshootingModel
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import retrofit2.http.GET

interface ApiServices {

    @GET("notifications.json")
    fun getNotificationDetails(): LiveData<Resource<NotificationSource>>

    @GET("faqResponse.json")
    fun getFaqDetails(): LiveData<Resource<FaqModel>>

    @GET("troubleshooting.json")
    fun getTroubleshootingDetails(): LiveData<Resource<TroubleshootingModel>>
}