package com.centurylink.biwf.repos

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.NetworkResource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.service.network.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun getNotificationDetails():
            LiveData<Resource<NotificationSource>> {
        return object : NetworkResource<NotificationSource>() {
            override fun createCall(): LiveData<Resource<NotificationSource>> {
                return apiServices.getNotificationDetails()
            }
        }.asLiveData()
    }
}