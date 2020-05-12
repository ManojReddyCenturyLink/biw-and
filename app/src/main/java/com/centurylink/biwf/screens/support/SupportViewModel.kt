package com.centurylink.biwf.screens.support

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SupportCoordinatorDestinations
import com.centurylink.biwf.model.support.FaqModel
import com.centurylink.biwf.model.support.FaqTopicsItem
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.SupportRepository
import com.centurylink.biwf.utility.EventFlow
import javax.inject.Inject

class SupportViewModel @Inject constructor(supportRepository: SupportRepository) : BaseViewModel() {

    val faqLiveData: MutableLiveData<MutableList<FaqTopicsItem>> = MutableLiveData()
    private var faqListDetails: LiveData<Resource<FaqModel>> = supportRepository.getFAQDetails()
    val myState = EventFlow<SupportCoordinatorDestinations>()

    fun getResponseData() = faqListDetails

    fun displaySortedNotifications(it: List<FaqTopicsItem>) {
        faqLiveData.value = it as MutableList<FaqTopicsItem>
    }

    fun navigateToFAQList(faqtopicsItem: FaqTopicsItem) {
        val bundle = Bundle()
        bundle.putString(FAQActivity.FAQ_TITLE, faqtopicsItem.type)
        SupportCoordinatorDestinations.bundle = bundle
        myState.latestValue = SupportCoordinatorDestinations.FAQ
    }

    fun runSpeedTest() {}

    fun restartModem() {}

    fun launchScheduleCallback() {
        myState.latestValue = SupportCoordinatorDestinations.SCHEDULE_CALLBACK
    }
}
