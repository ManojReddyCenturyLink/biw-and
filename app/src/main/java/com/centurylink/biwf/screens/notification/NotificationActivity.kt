package com.centurylink.biwf.screens.notification

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
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
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import javax.inject.Inject


/**
 * Activity for displaying the Notification Lists in the view
 */
class NotificationActivity : BaseActivity(), NotificationItemClickListener {

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        const val KEY_READ_HEADER: String = "READ_HEADER"
        fun newIntent(context: Context) = Intent(context, NotificationActivity::class.java)
    }

    @Inject
    lateinit var notificationCoordinator: NotificationCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val notificationViewModel by lazy {
        ViewModelProvider(this, factory).get(NotificationViewModel::class.java)
    }

    private lateinit var binding: ActivityNotificationBinding
    private var mergedNotificationList: MutableList<Notification> = mutableListOf()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        notificationViewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
            displayClearAllEvent.handleEvent { displayClearAllDialog() }
        }
        notificationCoordinator.observeThis(notificationViewModel.myState)
        initView()
        getNotificationInformation()
    }

    override fun onResume() {
        super.onResume()
        notificationCoordinator.navigator.activity = this
    }

    override fun onNotificationItemClick(notificationItem: Notification) {
        notificationViewModel.notificationItemClicked(notificationItem)
    }

    override fun clearAllReadNotification() {
        notificationViewModel.displayClearAllDialogs()
    }

    override fun markAllNotificationAsRead() {
        notificationViewModel.markNotificationasRead()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            NotificationDetailsActivity.requesttodismiss->{
                if(resultCode==Activity.RESULT_OK){
                    finish()
                }
            }
        }
    }

    private fun initView() {
        binding.notificationListRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val myDivider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        myDivider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_notification)!!)
        binding.notificationListRecyclerview.addItemDecoration(myDivider)
        binding.notificationListCloseicon.setOnClickListener { finish() }
    }

    private fun getNotificationInformation() {
        notificationViewModel.getNotificationDetails().observe(this) {
            when {
                it.status.isLoading() -> {

                }
                it.status.isSuccessful() -> {
                    notificationViewModel.displaySortedNotifications(it.data!!.notificationlist)
                    displaySortedNotification()
                }
                it.status.isError() -> {
                    notificationViewModel.displayErrorDialog()
                }
            }
        }
    }

    private fun displayClearAllDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.notification_screen_warning)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_yes, DialogInterface.OnClickListener { dialog, id ->
                performClearAll()
            })
            .setNegativeButton(R.string.dialog_no, DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })
        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(R.string.dialog_notiifcation_title)
        // show alert dialog
        alert.show()
    }

    private fun performClearAll() {
        notificationViewModel.clearAllReadNotifications()
    }

    private fun displaySortedNotification() {
        notificationViewModel.getNotificationMutableLiveData().observe(this, Observer {
            prepareRecyclerView(it)
        })
    }

    private fun prepareRecyclerView(notificationList: MutableList<Notification>) {
        mergedNotificationList = notificationList
        notificationAdapter = NotificationAdapter(notificationList, this)
        binding.notificationListRecyclerview.adapter = notificationAdapter
    }

}


