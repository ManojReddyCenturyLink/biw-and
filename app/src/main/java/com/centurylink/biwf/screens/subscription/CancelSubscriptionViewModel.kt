package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.CancelSubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import java.util.*
import javax.inject.Inject

class CancelSubscriptionViewModel @Inject constructor(
    private val cancelSubscriptionRepository:CancelSubscriptionRepository
) : BaseViewModel() {
    val cancelSubscriptionDate: EventLiveData<Date> = MutableLiveData()

    fun onCancelSubscription() {
    }

    fun getCancellationValidity(){
        val validityDate: Date = cancelSubscriptionRepository.getSubscriptionValidity()
        cancelSubscriptionDate.emit(validityDate)
    }
}