package com.centurylink.biwf.screens.home.dashboard

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.coordinators.NotificationCoordinator
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.CurrentAppointmentRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val currentAppointmentRepository: CurrentAppointmentRepository,
    notificationRepository: NotificationRepository
) : BaseViewModel() {

    val myState = ObservableData(DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED)
    private var accountID: String? = null
    private var notificationListDetails: LiveData<Resource<NotificationSource>> =
        notificationRepository.getNotificationDetails()
    val notificationLiveData:MutableLiveData<MutableList<Notification>> = MutableLiveData()
    private val unreadItem: Notification =
        Notification(DashboardFragment.KEY_UNREAD_HEADER, "",
            "", "", true, "")
    fun getNotificationDetails() = notificationListDetails
    private var mergedNotificationList: MutableList<Notification> = mutableListOf()

    fun getChangeAppointment(){
        myState.value = DashboardCoordinatorDestinations.CHANGE_APPOINTMENT
    }

    fun displaySortedNotifications(notificationList: List<Notification>) {
        val unreadNotificationList: MutableList<Notification>  = notificationList.asSequence()
            .filter { it.isUnRead }
            .toMutableList()
        mergedNotificationList.addAll(unreadNotificationList)
        notificationLiveData.value = unreadNotificationList
    }

    fun getNotificationMutableLiveData(): MutableLiveData<MutableList<Notification>> {
        return notificationLiveData
    }

    fun markNotificationAsRead(notificationItem:Notification) {
        if(notificationItem.isUnRead) {
            mergedNotificationList.remove(notificationItem)
            notificationItem.isUnRead = false
            mergedNotificationList.add(mergedNotificationList.size, notificationItem)
            val unreadNotificationList = mergedNotificationList.asSequence().filter { it.isUnRead }.toMutableList()
            if (unreadNotificationList.size == 1) {
                mergedNotificationList.remove(unreadItem)
            }
            notificationLiveData.value = unreadNotificationList
        }
    }

     fun navigateToNotificationDetails(notificationItem: Notification){
        var bundle= Bundle()
        bundle.putString(NotificationDetailsActivity.URL_TO_LAUNCH,notificationItem.detialUrl)
        bundle.putBoolean(NotificationDetailsActivity.LAUNCH_FROM_HOME,true)
        NotificationCoordinator.NotificationCoordinatorDestinations.bundle = bundle
        myState.value = DashboardCoordinatorDestinations.NOTIFICATION_DETAILS
    }

    /**
     * param: accountID as input
     * Function to fetch Current Appointment details from api
    */
    fun getCurrentStatus(){
        currentAppointmentRepository.getCurrentAppointment(accountId = "")
        myState.value = DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED
    }
}