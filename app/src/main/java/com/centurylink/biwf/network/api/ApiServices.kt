package com.centurylink.biwf.network.api

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.network.Resource
import retrofit2.http.GET

interface ApiServices {

    @GET("notifications.json")
    fun getNotificationDetails(): LiveData<Resource<NotificationSource>>

    @GET("faq.json")
    fun getFAQDetails(): LiveData<Resource<FAQ>>
}