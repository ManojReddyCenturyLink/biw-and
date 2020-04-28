package com.centurylink.biwf.screens.subscription

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionsDetailsCoordinator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionDetailsBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CancelSubscriptionDetailsActivity : BaseActivity() {

    companion object {
        const val REQUEST_TO__CANCEL_SUBSCRIPTION: Int = 11101
        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionDetailsActivity::class.java)
        }
    }

    @Inject
    lateinit var cancelSubscriptionDetailsCoordinator: CancelSubscriptionsDetailsCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val cancelSubscriptionDetailsModel by lazy {
        ViewModelProvider(this, factory).get(CancelSubscriptionDetailsViewModel::class.java)
    }
    private lateinit var binding: ActivityCancelSubscriptionDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelSubscriptionDetailsBinding.inflate(layoutInflater)
        cancelSubscriptionDetailsModel.apply {

        }
        setContentView(binding.root)
        initHeaders()
        initViews()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        cancelSubscriptionDetailsCoordinator.navigator.activity = this
    }

    private fun initHeaders() {
        binding.activityHeaderView.subheaderCenterTitle.text =
            getString(R.string.cancel_subscription_details_title)
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subheaderRightActionTitle.text =
            getText(R.string.text_header_cancel)
        binding.activityHeaderView.subheaderRightActionTitle.setOnClickListener {
           // setResult(Activity.RESULT_OK)
            //this.finish()
            displayDatePicker()
        }
    }

    private fun initViews(){
        val changeList = listOf("Others","Switched to New Plan","Charge Issues")
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, changeList)
        binding.cancellationReasonDropdown.adapter=adapter
    }

    private fun displayDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

        }, year, month, day)
        dpd.datePicker.minDate = c.getTimeInMillis();
        dpd.show()
    }

    private fun display(){

    }
}