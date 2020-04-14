package com.centurylink.biwf.screens.subscription

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.ManageSubscriptionCoordinator
import com.centurylink.biwf.databinding.ActivityManageSubscriptionBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class ManageSubscriptionActivity : BaseActivity() {

    companion object {
        const val requestToSubscription: Int = 1101
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, ManageSubscriptionActivity::class.java)
        }
    }

    @Inject
    lateinit var manageSubscriptionCoordinator: ManageSubscriptionCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val manageSubscriptionViewModel by lazy {
        ViewModelProvider(this, factory).get(ManageSubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivityManageSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityManageSubscriptionBinding.inflate(layoutInflater)
        manageSubscriptionViewModel.apply {
            cancelSubscriptionEvent.handleEvent { displayCancelSubscriptionDialog() }
        }
        setContentView(binding.root)
        setHeightofActivity()
        initHeaders()
    }
    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        manageSubscriptionCoordinator.navigator.activity = this
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.manage_subscription_title)
        binding.activityHeaderView.subHeaderTitle.text = screenTitle
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subHeaderRightIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
        getNextWeekDate()
        binding.cancelSubscription.setOnClickListener{manageSubscriptionViewModel.onCancelSubscription()}
    }

    private fun displayCancelSubscriptionDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.cancel_subscription_confirm)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel_subscription_ok, DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                binding.cancelSubscription.visibility= View.GONE
            })
        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(R.string.cancel_subscription_sub_header)
        // show alert dialog
        alert.show()
    }

    fun getNextWeekDate(){
        val cancelSubscriptionDate :Date = Calendar.getInstance().run {
            add(Calendar.DATE, 7)
            time
        }
        var formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        //var formattedDate = cancelSubscriptionDate.format(formatter)
    }
}