package com.centurylink.biwf.screens.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
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
 * Notification activity - Activity for displaying the Notification Lists in the view
 *
 * @constructor Create empty Notification activity
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

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
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

    /**
     * On notification item click - It handles notification item click listener
     *
     * @param notificationItem - returns notification item clicked
     */
    override fun onNotificationItemClick(notificationItem: Notification) {
        viewModel.notificationItemClicked(notificationItem)
    }

    /**
     * Clear all read notification - It will display all read notification dialog from list of
     * read-notifications
     *
     */
    override fun clearAllReadNotification() {
        viewModel.displayClearAllDialogs()
    }

    /**
     * Mark all notification as read - It will mark all unread notification as read
     *
     */
    override fun markAllNotificationAsRead() {
        viewModel.markNotificationasRead()
    }

    /**
     * On back pressed - This will handle back key click listeners
     *
     */
    override fun onBackPressed() {
        finish()
    }

    /**
     * On activity result - Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned and any additional data from it.
     *
     * @param requestCode - It is originally supplied to startActivityForResult(), allowing
     * to identify result code came from.
     * @param resultCode - It is returned by the child activity through its setResult().
     * @param data - It will return result data to the caller activity.
     */
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

    /**
     * Init headers - It will initialize screen headers
     *
     */
    private fun initHeaders() {
        val screenTitle: String = getString(R.string.notification_details)
        binding.incHeader.apply {
            subHeaderLeftIcon.visibility = View.GONE
            subheaderCenterTitle.text = screenTitle
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                finish()
            }
        }
    }

    /**
     * Init view - It will initialize activity views
     *
     */
    private fun initView() {
        binding.notificationListRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    /**
     * Get notification information - It shows list of sorted notifications
     *
     */
    private fun getNotificationInformation() {
        viewModel.notificationListDetails.observe {
            viewModel.displaySortedNotifications(it.notificationlist)
            displaySortedNotification()
        }
    }

    /**
     * Display clear all dialog - It will handle clear all click listener by showing error pop-up
     *
     */
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

    /**
     * Perform clear all - It is used to remove read notifications by performing clear all
     *
     */
    private fun performClearAll() {
        viewModel.clearAllReadNotifications()
    }

    /**
     * Display sorted notification - It handles sorting notification logic
     *
     */
    private fun displaySortedNotification() {
        viewModel.notifications.observe {
            prepareRecyclerView(it)
        }
    }

    /**
     * Prepare recycler view
     *
     * @param notificationList - returns list of notifications
     */
    private fun prepareRecyclerView(notificationList: MutableList<Notification>) {
        mergedNotificationList = notificationList
        notificationAdapter = NotificationAdapter(notificationList, this)
        binding.notificationListRecyclerview.adapter = notificationAdapter
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        const val KEY_READ_HEADER: String = "READ_HEADER"

        fun newIntent(context: Context) = Intent(context, NotificationActivity::class.java)
    }
}
