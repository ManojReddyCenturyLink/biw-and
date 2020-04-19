package com.centurylink.biwf.screens.subscription

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class CancelSubscriptionActivity : BaseActivity() {

    companion object {
        const val REQUEST_TO_SUBSCRIPTION: Int = 1101
        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionActivity::class.java)
        }
    }

    @Inject
    lateinit var cancelSubscriptionCoordinator: CancelSubscriptionCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val manageSubscriptionViewModel by lazy {
        ViewModelProvider(this, factory).get(CancelSubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivityCancelSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityCancelSubscriptionBinding.inflate(layoutInflater)
        manageSubscriptionViewModel.apply {
            cancelSubscriptionEvent.handleEvent { displayCancelSubscriptionDialog() }
            cancelSubscriptionDate.handleEvent { displayCancellationValidity(it) }
        }
        setContentView(binding.root)
        setHeightofActivity()
        initHeaders()
        manageSubscriptionViewModel.getCancellationValidity()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        cancelSubscriptionCoordinator.navigator.activity = this
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.cancel_subscription_title)
        binding.activityHeaderView.subHeaderTitle.text = screenTitle
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subHeaderRightIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
        binding.cancelSubscription.setOnClickListener { manageSubscriptionViewModel.onCancelSubscription() }
    }

    private fun displayCancelSubscriptionDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.cancel_subscription_confirm)
            .setCancelable(true)
            .setNegativeButton(
                R.string.cancel_subscription_ok,
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                    binding.cancelSubscription.visibility = View.GONE
                })
        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(R.string.cancel_subscription_sub_header)
        // show alert dialog
        alert.show()
    }

    @SuppressLint("StringFormatInvalid")
    private fun displayCancellationValidity(date: Date) {
        val validityDate = DateFormat.getDateInstance(DateFormat.LONG).format(date)
        binding.manageSubscriptionContent.text =
            getString(R.string.manage_subscription_content, validityDate)
    }
}