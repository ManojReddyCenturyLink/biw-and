package com.centurylink.biwf.screens.home.account.subscription

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SubscriptionCoordinator
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.screens.subscription.SubscriptionStatementActivity
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.ObservableData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val zuoraPaymentRepository: ZuoraPaymentRepository
) : BaseViewModel() {

    init {
        getInvoicesList()
    }

    val invoicesListResponse: Flow<PaymentList> = BehaviorStateFlow()
    val myState =
        ObservableData(SubscriptionCoordinator.SubscriptionCoordinatorDestinations.SUBSCRIPTION)

    private fun getInvoicesList() {
        viewModelScope.launch {
            try {
                val paymentList = zuoraPaymentRepository.getInvoicesList()
                invoicesListResponse.latestValue = paymentList
            } catch (e: Throwable) {
            }
        }
    }

    fun launchStatement(item: RecordsItem) {
        var bundle = Bundle()
        bundle.putString(
            SubscriptionStatementActivity.SUBSCRIPTION_STATEMENT_TITLE,
            item.id
        )
        SubscriptionCoordinator.SubscriptionCoordinatorDestinations.bundle = bundle
        myState.value = SubscriptionCoordinator.SubscriptionCoordinatorDestinations.STATEMENT
    }

    fun launchManageSubscription() {
        myState.value =
            SubscriptionCoordinator.SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION
    }
}