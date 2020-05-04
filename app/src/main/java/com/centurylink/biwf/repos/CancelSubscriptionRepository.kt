package com.centurylink.biwf.repos

import com.centurylink.biwf.service.network.ApiServices
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelSubscriptionRepository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun getSubscriptionValidity(): Date {
        Calendar.getInstance().apply {
            add(Calendar.DATE, 7)
            return time
        }
    }
}