package com.centurylink.biwf.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.model.Subscription
import com.centurylink.biwf.service.network.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val apiServices: ApiServices
) {

    fun getSubscription(): LiveData<Subscription> {
        return MutableLiveData(
            Subscription(
                subscriptionName = "Best in World Fiber",
                subscriptionDetails = "Speeds up to 940Mbps",
                subscriptionDate = "04/01/20",
                subscriptionCardDigits = "1234",
                subscriptionCardType = "Visa"
            )
        )
    }
}