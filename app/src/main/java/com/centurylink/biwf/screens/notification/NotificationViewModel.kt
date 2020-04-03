package com.centurylink.biwf.screens.notification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.NotificationCoordinator
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    val errorEvents: EventLiveData<String> = MutableLiveData()

    val displayClearAllEvent: EventLiveData<Unit> = MutableLiveData()

    val myState = ObservableData(NotificationCoordinator.
        NotificationCoordinatorDestinations.NOTIFICATION_LIST)

    private val unreadItem: Notification =
        Notification(NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, "")

    private val readItem: Notification =
        Notification(NotificationActivity.KEY_READ_HEADER, "",
            "", "", false, "")

    private var mergedNotificationList: MutableList<Notification> = mutableListOf()

    /**
     * Loading Notification details from server
     */
    private var notificationListDetails: LiveData<Resource<NotificationSource>> =
        notificationRepository.getNotificationDetails()

    fun getNotificationDetails() = notificationListDetails

    fun notificationItemClicked(notificationItem:Notification) :MutableList<Notification>{
        if(notificationItem.isUnRead) {
            mergedNotificationList.remove(notificationItem)
            notificationItem.isUnRead = false
            mergedNotificationList.add(mergedNotificationList.size, notificationItem)
            val unreadNotificationList = mergedNotificationList.asSequence()
                .filter { it.isUnRead }
                .toMutableList()
            if (unreadNotificationList.size == 1) {
                mergedNotificationList.remove(unreadItem)
            }
        }
         return mergedNotificationList
    }

    fun navigatetoNotiifcationDetails(){
        myState.value =
            NotificationCoordinator.NotificationCoordinatorDestinations.NOTIFICATION_DETAILS
    }

    fun markNotificationasRead(): MutableList<Notification> {
        mergedNotificationList.forEach { it.isUnRead = false }
        mergedNotificationList.remove(unreadItem)
        mergedNotificationList.remove(readItem)
        mergedNotificationList.add(0, readItem)
        return mergedNotificationList
    }

    fun displaySortedNotification(notificationList: List<Notification>): MutableList<Notification> {
        val unreadNotificationList = notificationList.asSequence()
            .filter { it.isUnRead }
            .toMutableList()
        if (unreadNotificationList.size > 0) {
            unreadNotificationList.add(0, unreadItem)
        }
        val readNotificationList = notificationList.asSequence()
            .filter { !it.isUnRead }
            .toMutableList()
        if (readNotificationList.size > 0) {
            readNotificationList.add(0, readItem)
        }
        mergedNotificationList.addAll(unreadNotificationList)
        mergedNotificationList.addAll(unreadNotificationList.size, readNotificationList)
        return mergedNotificationList
    }
    
    fun clearAllReadNotifications(): MutableList<Notification> {
        mergedNotificationList = mergedNotificationList.filter { it.isUnRead }.toMutableList()
        mergedNotificationList.remove(readItem)
        return mergedNotificationList
    }

    fun displayClearAllDialogs(){
        displayClearAllEvent.emit(Unit)
    }

    fun displayErrorDialog(){
        errorEvents.emit("Server error!Try again later")
    }

}