package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinator
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.CancelSubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ObservableData
import java.util.*
import javax.inject.Inject

class CancelSubscriptionViewModel @Inject constructor(
    private val cancelSubscriptionRepository:CancelSubscriptionRepository
) : BaseViewModel() {

    val cancelSubscriptionDate: EventLiveData<Date> = MutableLiveData()


    val myState = ObservableData(CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations.CANCEL_SUBSCRIPTION)

    fun getCancellationValidity(){
        val validityDate: Date = cancelSubscriptionRepository.getSubscriptionValidity()
        cancelSubscriptionDate.emit(validityDate)
    }

    private fun navigateTo(destinations: CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations) {
        when (destinations) {
            CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations.CANCEL_SUBSCRIPTION->{}
            CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION -> {
                onNavigateToCancelSubscriptionDetails()
            }
        }
    }

    fun onNavigateToCancelSubscriptionDetails() {
        myState.value =
            CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION
    }

}