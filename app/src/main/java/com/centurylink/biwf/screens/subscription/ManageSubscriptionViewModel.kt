package com.centurylink.biwf.screens.subscription

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.FAQRepository
import javax.inject.Inject

class ManageSubscriptionViewModel @Inject constructor(
    private val faqRepository: FAQRepository
) : BaseViewModel() {

}