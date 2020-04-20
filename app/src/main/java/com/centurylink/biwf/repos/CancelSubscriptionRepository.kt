package com.centurylink.biwf.repos

import com.centurylink.biwf.network.api.ApiServices
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelSubscriptionRepository @Inject constructor(
    private val apiServices: ApiServices
) {

    fun cancelSubscription(): Boolean {
        return true
    }

    fun getSubscriptionValidity(): Date {
        Calendar.getInstance().apply {
            add(Calendar.DATE, 7)
            return time
        }
    }
}