package com.centurylink.biwf.screens.support.schedulecallback

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.ScheduleCallbackResponse
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.model.support.Values
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.repos.ScheduleCallbackRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Schedule callback view model
 *
 * @property scheduleCallbackRepository - repository instance to call the schedule callback service
 * @property caseRepository - repository instance to fetch record type id
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class ScheduleCallbackViewModel @Inject constructor(
    private val scheduleCallbackRepository: ScheduleCallbackRepository,
    private val caseRepository: CaseRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val myState = EventFlow<ScheduleCallbackCoordinatorDestinations>()
    var errorMessageFlow = EventFlow<String>()
    var isExistingUserState: Boolean = false
    private var scheduleCallbackPicklist: ScheduleCallbackPicklist = ScheduleCallbackPicklist()
    var arrayList: MutableList<String> = mutableListOf()
    var progressViewFlow = EventFlow<Boolean>()
    private var recordTypeIdValue: String? = ""

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {

        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SCHEDULE_CALLBACK_SUPPORT)
        initApiCalls()
    }

    fun initApiCalls() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestRecordId()
            requestScheduleCallbackPicklist()
        }
    }

    /**
     * Launch call dialer - It is used to launch caller dialer logic
     *
     */
    fun launchCallDialer() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CALL_US_SCHEDULE_CALLBACK)
        myState.latestValue = ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT
    }

    /**
     * Navigate additional info screen - It will handle navigation to additional info screen logic
     *on select of item
     *
     * @param item - The item selected for navigation
     */
    fun navigateAdditionalInfoScreen(item: TopicList) {
        analyticsManagerInterface.logListItemClickEvent(AnalyticsKeys.LIST_ITEM_SCHEDULE_CALLBACK)
        ScheduleCallbackCoordinatorDestinations.bundle = Bundle().apply {
            putString(AdditionalInfoActivity.ADDITIONAL_INFO, item.topic)
            putBoolean(AdditionalInfoActivity.IS_EXISTING_USER, isExistingUserState)
        }
        myState.latestValue = ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO
    }

    /**
     * Set is existing user state
     *
     * @param isExistingUser - The boolean value to set existing user
     * Its true for existing user and false for non existing user
     */
    fun setIsExistingUserState(isExistingUser: Boolean) {
        isExistingUserState = isExistingUser
    }

    /**
     * Request record id - suspend function used to fetch record type id
     *
     */
    private suspend fun requestRecordId() {
        val subscriptionDate = caseRepository.getRecordTypeId()
        subscriptionDate.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_RECORD_TYPE_ID_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_RECORD_TYPE_ID_SUCCESS)
            recordTypeIdValue = it
        }
    }

    /**
     * Request schedule callback picklist - used to fetch customer care options picklist
     *
     */
    private suspend fun requestScheduleCallbackPicklist() {
        val picklist = scheduleCallbackRepository.scheduleCallbackInfo(recordTypeIdValue)
        picklist.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateScheduleCallbackPicklist(it)
        }
    }

    /**
     * Update schedule picklist - used to populate the list of customer care options
     *
     * @param response - response received from API according to record type id
     */
    fun updateScheduleCallbackPicklist(response: ScheduleCallbackResponse) {
        scheduleCallbackPicklist = scheduleCallbackPicklist.copy(values = response.values)
        for (str in scheduleCallbackPicklist.values) {
            str.label = str.label.replace("&#39;","'")
            arrayList.add(str.label)
        }
        progressViewFlow.latestValue = false
    }

    /**
     * Log back button click - It will handle back button click event logic
     *
     */
    fun logBackButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_SCHEDULE_CALLBACK)
    }

    /**
     * Log cancel button click - It will handle cancel button click event logic
     *
     */
    fun logCancelButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_SCHEDULE_CALLBACK)
    }

    /**
     * Schedule callback picklist
     *
     * @property values - used to store the attributes of each picklist item
     * @constructor Create empty Schedule callback picklist
     */
    data class ScheduleCallbackPicklist(
        var values: List<Values> = emptyList()
    )

}
