package com.centurylink.biwf.repos

import com.centurylink.biwf.network.api.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageSubscriptionRepository @Inject constructor(
    private val apiServices: ApiServices
) {

    fun cancelSubscription(): Boolean {
        return true
    }
}