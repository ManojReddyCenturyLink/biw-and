package com.centurylink.biwf.screens.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.model.support.FaqModel
import com.centurylink.biwf.model.support.FaqTopicsItem
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.SupportRepository
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class SupportViewModel @Inject constructor(supportRepository: SupportRepository) : BaseViewModel() {

    private val faqLiveData:MutableLiveData<MutableList<FaqTopicsItem>> = MutableLiveData()
    private var faqListDetails: LiveData<Resource<FaqModel>> = supportRepository.getFAQDetails()
    val myState = ObservableData(SupportCoordinator.SupportCoordinatorDestinations.SUPPORT)

    fun getResponseData() = faqListDetails

    fun displaySortedNotifications(it: List<FaqTopicsItem>) {
        faqLiveData.value = it as MutableList<FaqTopicsItem>
    }

    fun getSupportFaqMutableLiveData(): MutableLiveData<MutableList<FaqTopicsItem>> {
        return faqLiveData
    }

    fun runSpeedTest(){}

    fun restartModem(){}

    fun callUs(){}
}


