package com.centurylink.biwf.screens.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.NotificationCoordinator
import com.centurylink.biwf.databinding.ActivityNotificationBinding
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.screens.notification.adapter.NotificationAdapter
import com.centurylink.biwf.screens.notification.adapter.NotificationItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject


/**
 * Activity for displaying the Notification Lists in the view
 */
class NotificationActivity : BaseActivity(), NotificationItemClickListener {

    @Inject
    lateinit var notificationCoordinator: NotificationCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    override val viewModel by lazy {
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
        navigator.observe(this)
        setActivityHeight()

        viewModel.apply {
            displayClearAllEvent.observe { displayClearAllDialog() }
        }
        viewModel.myState.observeWith(notificationCoordinator)

        initView()
        initHeaders()
        getNotificationInformation()
    }

    override fun onNotificationItemClick(notificationItem: Notification) {
        viewModel.notificationItemClicked(notificationItem)
    }

    override fun clearAllReadNotification() {
        viewModel.displayClearAllDialogs()
    }

    override fun markAllNotificationAsRead() {
        viewModel.markNotificationasRead()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            NotificationDetailsActivity.REQUEST_TO_DISMISS -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.notification_details)
        binding.incHeader.apply {
            subHeaderLeftIcon.visibility = View.GONE
            subheaderCenterTitle.text = screenTitle
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                finish()
            }
        }
    }

    private fun initView() {
        binding.notificationListRecyclerview.layoutManager =
           LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun getNotificationInformation() {
        viewModel.notificationListDetails.observe {
            viewModel.displaySortedNotifications(it.notificationlist)
            displaySortedNotification()
        }
    }

    private fun displayClearAllDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.notification_screen_warning)
            .setCancelable(true)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                performClearAll()
            }
            .setNegativeButton(R.string.dialog_no) { dialog, _ ->
                dialog.cancel()
            }
        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(R.string.dialog_notiifcation_title)
        // show alert dialog
        alert.show()
    }

    private fun performClearAll() {
        viewModel.clearAllReadNotifications()
    }

    private fun displaySortedNotification() {
        viewModel.notifications.observe {
            prepareRecyclerView(it)
        }
    }

    private fun prepareRecyclerView(notificationList: MutableList<Notification>) {
        mergedNotificationList = notificationList
        notificationAdapter = NotificationAdapter(notificationList, this)
        binding.notificationListRecyclerview.adapter = notificationAdapter
    }

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        const val KEY_READ_HEADER: String = "READ_HEADER"

        fun newIntent(context: Context) = Intent(context, NotificationActivity::class.java)
    }
}


