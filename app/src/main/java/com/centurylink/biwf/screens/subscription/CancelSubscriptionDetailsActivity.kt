package com.centurylink.biwf.screens.subscription

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionsDetailsCoordinator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionDetailsBinding
import com.centurylink.biwf.screens.subscription.adapter.CancellationReasonAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.willy.ratingbar.BaseRatingBar
import java.text.DateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CancelSubscriptionDetailsActivity : BaseActivity() {

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
            errorEvents.handleEvent { displayDateError() }
            performSubmitEvent.handleEvent { showCancellationDialog() }
            cancelSubscriptionDateEvent.handleEvent { updateCancellationDate(it) }
            displayReasonSelectionEvent.handleEvent { toggleCancellationReasonInfo(it) }
            changeDateEvent.handleEvent { displayDatePicker() }
        }
        setContentView(binding.root)
        initHeaders()
        initTextWatchers()
        initSpinner()
        initRatingView()
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
            setResult(Activity.RESULT_OK)
            this.finish()
        }
        binding.cancelSubscriptionSubmit.setOnClickListener{
            cancelSubscriptionDetailsModel.onSubmitCancellation()}
    }

    private fun initTextWatchers() {
        binding.cancellationSpecifyReasonInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editText: Editable?) {
                cancelSubscriptionDetailsModel.onOtherCancellationChanged(editText.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }
        })
        binding.cancellationCommentsReasonInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editText: Editable?) {
                cancelSubscriptionDetailsModel.onCancellationCommentsChanged(editText.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }
        })
    }

    private fun toggleCancellationReasonInfo(show: Boolean) {
        val state = if (show) View.VISIBLE else View.GONE
        binding.cancellationReasonOptionalLabel.visibility = state
        binding.cancellationSpecifyReasonLabel.visibility = state
        binding.cancellationSpecifyReasonInput.visibility = state
    }

    private fun updateCancellationDate(date: Date) {
        val validityDate = DateFormat.getDateInstance(DateFormat.LONG).format(date)
        binding.cancellationDateSelection.text = validityDate
    }

    private fun initSpinner() {
        val changeList: ArrayList<String> = ArrayList()
        changeList.addAll(
            listOf(
                "Moving",
                "Service is not working as expected",
                "Price is too high",
                "Switching service providers",
                "Other"
            )
        )
        val adapter = CancellationReasonAdapter(this, changeList)
        binding.cancellationReasonDropdown.adapter = adapter
        binding.cancellationReasonDropdown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    cancelSubscriptionDetailsModel.onCancellationReason(changeList[position])
                }
            }
    }

    private fun displayDatePicker() {
        binding.cancelSubscriptionDetailsError.visibility = View.GONE
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                cancelSubscriptionDetailsModel.onCancellationDateSelected(newDate.time)
            },
            year,
            month,
            day
        )
        datePicker.datePicker.minDate = c.timeInMillis
        datePicker.show()
    }

    private fun initRatingView() {
        binding.cancellationDateSelection.setOnClickListener { cancelSubscriptionDetailsModel.onDateChange() }
        binding.cancellationServiceRatingBar.setOnRatingChangeListener { baseRatingBar: BaseRatingBar, rating: Float, b: Boolean ->
            baseRatingBar.rating = rating
            cancelSubscriptionDetailsModel.onRatingChanged(rating)
        }
    }

    private fun displayDateError() {
        binding.cancelSubscriptionDetailsError.visibility = View.VISIBLE
    }

    private fun showCancellationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_cancel_subscription_details)
        val keepService =
            dialog.findViewById<TextView>(R.id.cancellation_detail_dialog_keep_service)
        val cancelService =
            dialog.findViewById<TextView>(R.id.cancellation_detail_dialog_cancel_service)
        keepService.setOnClickListener {
            dialog.dismiss()

        }
        cancelService.setOnClickListener {
            cancelSubscriptionDetailsModel.performCancellationCall()
            dialog.dismiss() }
        dialog.show()
    }

    companion object {
        const val REQUEST_TO__CANCEL_SUBSCRIPTION: Int = 11101
        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionDetailsActivity::class.java)
        }
    }
}