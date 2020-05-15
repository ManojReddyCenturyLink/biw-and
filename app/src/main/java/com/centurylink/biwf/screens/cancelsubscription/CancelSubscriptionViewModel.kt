package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinatorDestinations
import com.centurylink.biwf.repos.CancelSubscriptionRepository
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import java.util.Date
import javax.inject.Inject

class CancelSubscriptionViewModel @Inject constructor(
    private val cancelSubscriptionRepository:CancelSubscriptionRepository
) : BaseViewModel() {

    val cancelSubscriptionDate: EventLiveData<Date> = MutableLiveData()


    val myState = EventFlow<CancelSubscriptionCoordinatorDestinations>()

    fun getCancellationValidity(){
        val validityDate: Date = cancelSubscriptionRepository.getSubscriptionValidity()
        cancelSubscriptionDate.emit(validityDate)
    }

    fun onNavigateToCancelSubscriptionDetails() {
        myState.latestValue =
            CancelSubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION
    }
}
