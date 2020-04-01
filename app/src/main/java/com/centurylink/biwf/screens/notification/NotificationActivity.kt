package com.centurylink.biwf.screens.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.NotificationCoordinator
import com.centurylink.biwf.databinding.ActivityNotificationBinding
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.screens.notification.adapter.NotificationAdapter
import com.centurylink.biwf.screens.notification.adapter.NotificationItemClickListener
import com.centurylink.biwf.screens.notification.viewmodel.NotificationViewModel
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe

import javax.inject.Inject


/**
 * Activity for displaying the Notification Lists in the view
 */
class NotificationActivity : BaseActivity(), NotificationItemClickListener {

    @Inject
    lateinit var notificationCoordinator: NotificationCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private lateinit var binding: ActivityNotificationBinding

    private val notificationViewModel by lazy {
        ViewModelProvider(this, factory).get(NotificationViewModel::class.java)
    }

    private var mergedNotificationList: MutableList<Notification> = mutableListOf()

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        notificationCoordinator.navigator.activity = this
        notificationViewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
        }
        notificationCoordinator.observeThis(notificationViewModel.myState)
        initView()
        getNotificationInformation()
    }

    private fun initView() {
        binding.notificationList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.notificationList.addItemDecoration(myDivider)
        binding.ivCloseIcon.setOnClickListener { finish() }
    }

    private fun getNotificationInformation() {
        notificationViewModel.getNotificationDetails().observe(this){
            when {
                it.status.isLoading() -> {

                }
                it.status.isSuccessful() -> {
                    displaySortedNotification(notificationViewModel.displaySortedNotification(it.data!!.notificationlist))
                }
                it.status.isError() -> {
                }
            }
        }
    }

    private fun displaySortedNotification(notificationList: MutableList<Notification>) {
        //creating Mock Listitem for Each List Header
        mergedNotificationList =notificationList
        notificationAdapter = NotificationAdapter(mergedNotificationList,this)
        binding.notificationList.adapter = notificationAdapter
        notificationAdapter.notifyDataSetChanged()
    }

    override fun onNotificationItemClick(notificationItem: Notification) {
        notificationViewModel.notificationItemClicked()
    }

    override fun clearAllReadNotification() {
        mergedNotificationList = notificationViewModel.clearAllReadNotifications()
        notificationAdapter.updateList(mergedNotificationList)
    }

    override fun markAllNotificationAsRead() {
        mergedNotificationList = notificationViewModel.markNotificationasRead()
        notificationAdapter.updateList(mergedNotificationList)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationCoordinator.navigator.activity = null
    }

    private fun displayToast(erroMessage: String) {
        Toast.makeText(this, erroMessage, Toast.LENGTH_SHORT).show()
    }



    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        const  val KEY_READ_HEADER: String = "READ_HEADER"
        fun newIntent(context: Context) = Intent(context, NotificationActivity::class.java)
    }
}


