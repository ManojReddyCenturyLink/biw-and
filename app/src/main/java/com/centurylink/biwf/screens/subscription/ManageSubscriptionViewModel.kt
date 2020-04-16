package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.ManageSubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import java.util.*
import javax.inject.Inject

class ManageSubscriptionViewModel @Inject constructor(
    private val manageSubscriptionRepository:ManageSubscriptionRepository
) : BaseViewModel() {
    val cancelSubscriptionEvent: EventLiveData<Unit> = MutableLiveData()
    val cancelSubscriptionDate: EventLiveData<Date> = MutableLiveData()

    fun onCancelSubscription() {
      if(manageSubscriptionRepository.cancelSubscription()){
          cancelSubscriptionEvent.emit(Unit)
      }
    }

    fun getCancellationValidity(){
        val validityDate: Date = manageSubscriptionRepository.getSubscriptionValidity()
        cancelSubscriptionDate.emit(validityDate)
    }
}