package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.ManageSubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import javax.inject.Inject

class ManageSubscriptionViewModel @Inject constructor(
    private val manageSubscriptionRepository:ManageSubscriptionRepository
) : BaseViewModel() {
    val cancelSubscriptionEvent: EventLiveData<Unit> = MutableLiveData()

    fun onCancelSubscription() {
      if(manageSubscriptionRepository.cancelSubscription()){
          cancelSubscriptionEvent.emit(Unit)
      }
    }
}