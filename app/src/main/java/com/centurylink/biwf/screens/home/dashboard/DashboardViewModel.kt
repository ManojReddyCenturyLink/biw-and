package com.centurylink.biwf.screens.home.dashboard

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.coordinators.NotificationCoordinatorDestinations
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    val myState = EventFlow<DashboardCoordinatorDestinations>()
    var notificationListDetails = BehaviorStateFlow<NotificationSource>()
    val notificationLiveData: BehaviorStateFlow<MutableList<Notification>> = BehaviorStateFlow()
    private val unreadItem: Notification =
        Notification(
            DashboardFragment.KEY_UNREAD_HEADER, "",
            "", "", true, ""
        )
    var appointmentStatusFlow = BehaviorStateFlow<String>()
    private var mergedNotificationList: MutableList<Notification> = mutableListOf()

    init {
        initApi()
    }

    private fun initApi() {
        viewModelScope.launch {
            requestNotificationDetails()
        }
    }

    private suspend fun requestNotificationDetails() {
        val result = notificationRepository.getNotificationDetails()
        notificationListDetails.latestValue = result
    }

    fun getChangeAppointment() {
        myState.latestValue = DashboardCoordinatorDestinations.CHANGE_APPOINTMENT
    }

    fun displaySortedNotifications(notificationList: List<Notification>) {
        val unreadNotificationList: MutableList<Notification> = notificationList.asSequence()
            .filter { it.isUnRead }
            .toMutableList()
        mergedNotificationList.addAll(unreadNotificationList)
        notificationLiveData.value = unreadNotificationList
    }

    fun getNotificationMutableLiveData(): BehaviorStateFlow<MutableList<Notification>> {
        return notificationLiveData
    }

    fun markNotificationAsRead(notificationItem: Notification) {
        if (notificationItem.isUnRead) {
            mergedNotificationList.remove(notificationItem)
            notificationItem.isUnRead = false
            mergedNotificationList.add(mergedNotificationList.size, notificationItem)
            val unreadNotificationList =
                mergedNotificationList.asSequence().filter { it.isUnRead }.toMutableList()
            if (unreadNotificationList.size == 1) {
                mergedNotificationList.remove(unreadItem)
            }
            notificationLiveData.value = unreadNotificationList
        }
    }

    fun navigateToNotificationDetails(notificationItem: Notification) {
        val bundle = Bundle()
        bundle.putString(NotificationDetailsActivity.URL_TO_LAUNCH, notificationItem.detialUrl)
        bundle.putBoolean(NotificationDetailsActivity.LAUNCH_FROM_HOME, true)
        NotificationCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.NOTIFICATION_DETAILS
    }
}
