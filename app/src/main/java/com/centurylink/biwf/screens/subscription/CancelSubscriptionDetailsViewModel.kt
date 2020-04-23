package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.CancelSubscriptionDetailsRepository
import com.centurylink.biwf.utility.EventLiveData
import java.util.*
import javax.inject.Inject

class CancelSubscriptionDetailsViewModel @Inject constructor(
    private val cancelSubscriptionDetailsRepository: CancelSubscriptionDetailsRepository
) : BaseViewModel() {
    val cancelSubscriptionDate: EventLiveData<Date> = MutableLiveData()

}