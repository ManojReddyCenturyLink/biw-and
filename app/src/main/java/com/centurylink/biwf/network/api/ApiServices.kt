package com.centurylink.biwf.network.api

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import retrofit2.http.GET

interface ApiServices {

    @GET("bins/16b1p0")
    fun getNotificationDetails(): LiveData<Resource<NotificationSource>>
}