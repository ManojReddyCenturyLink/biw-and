package com.centurylink.biwf.screens.notification

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.NotificationCoordinatorDestinations
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import javax.inject.Inject


class NotificationViewModel @Inject constructor(
    notificationRepository: NotificationRepository
) : BaseViewModel() {

    val errorEvents: EventLiveData<String> = MutableLiveData()
    val displayClearAllEvent: EventLiveData<Unit> = MutableLiveData()
    val myState = EventFlow<NotificationCoordinatorDestinations>()
    val notificationLiveData: MutableLiveData<MutableList<Notification>> = MutableLiveData()
    private val unreadItem: Notification =
        Notification(
            NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, ""
        )
    private val readItem: Notification =
        Notification(
            NotificationActivity.KEY_READ_HEADER, "",
            "", "", false, ""
        )

    private var mergedNotificationList: MutableList<Notification> = mutableListOf()
    private var notificationListDetails: LiveData<Resource<NotificationSource>> =
        notificationRepository.getNotificationDetails()

    fun getNotificationDetails() = notificationListDetails

    fun notificationItemClicked(notificationItem: Notification) {
        if (notificationItem.isUnRead) {
            mergedNotificationList.remove(notificationItem)
            notificationItem.isUnRead = false
            mergedNotificationList.add(mergedNotificationList.size, notificationItem)
            val readNotificationList =
                mergedNotificationList.asSequence().filter { !it.isUnRead }.toMutableList()
            if (readNotificationList.size == 1) {
                mergedNotificationList.add(mergedNotificationList.size - 1, readItem)
            }
            val unreadNotificationList = mergedNotificationList.asSequence()
                .filter { it.isUnRead }
                .toMutableList()
            if (unreadNotificationList.size == 1) {
                mergedNotificationList.remove(unreadItem)
            }

            notificationLiveData.value = mergedNotificationList
        }
        navigatetoNotifcationDetails(notificationItem)
    }

    private fun navigatetoNotifcationDetails(notificationItem: Notification) {
        val bundle = Bundle()
        bundle.putString(NotificationDetailsActivity.URL_TO_LAUNCH, notificationItem.detialUrl)
        bundle.putBoolean(NotificationDetailsActivity.LAUNCH_FROM_HOME, true)
        NotificationCoordinatorDestinations.bundle = bundle
        myState.latestValue = NotificationCoordinatorDestinations.NOTIFICATION_DETAILS
    }

    fun markNotificationasRead() {
        mergedNotificationList.forEach { it.isUnRead = false }
        mergedNotificationList.remove(unreadItem)
        mergedNotificationList.remove(readItem)
        mergedNotificationList.add(0, readItem)
        notificationLiveData.value = mergedNotificationList
    }

    fun displaySortedNotifications(notificationList: List<Notification>) {
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
        notificationLiveData.value = mergedNotificationList
    }

    fun clearAllReadNotifications() {
        mergedNotificationList = mergedNotificationList.filter { it.isUnRead }.toMutableList()
        mergedNotificationList.remove(readItem)
        notificationLiveData.value = mergedNotificationList
    }

    fun displayClearAllDialogs() {
        displayClearAllEvent.emit(Unit)
    }

    fun displayErrorDialog() {
        errorEvents.emit("Server error!Try again later")
    }

    fun getNotificationMutableLiveData(): MutableLiveData<MutableList<Notification>> {
        return notificationLiveData
    }
}