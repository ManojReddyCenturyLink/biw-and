package com.centurylink.biwf.screens.cancelsubscription

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
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionDetailsBinding
import com.centurylink.biwf.databinding.DialogCancelSubscriptionDetailsBinding
import com.centurylink.biwf.screens.cancelsubscription.adapter.CancellationReasonAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.willy.ratingbar.BaseRatingBar
import java.text.DateFormat
import java.util.*
import javax.inject.Inject


class CancelSubscriptionDetailsActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private var dialog: Dialog? = null

    private val cancelSubscriptionDetailsModel by lazy {
        ViewModelProvider(this, factory).get(CancelSubscriptionDetailsViewModel::class.java)
    }
    private lateinit var binding: ActivityCancelSubscriptionDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelSubscriptionDetailsBinding.inflate(layoutInflater)
        cancelSubscriptionDetailsModel.apply {
            errorEvents.handleEvent { displayDateError() }
            performSubmitEvent.handleEvent { showCancellationDialog(it) }
            cancelSubscriptionDateEvent.handleEvent { updateCancellationDate(it) }
            displayReasonSelectionEvent.handleEvent { toggleCancellationReasonInfo(it) }
            changeDateEvent.handleEvent { displayDatePicker() }
        }
        setContentView(binding.root)
        initHeaders()
        initTextWatchers()
        observeViews()
        initSpinner()
        initRatingView()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        binding.activityHeaderView.apply {
            subheaderCenterTitle.text =
                getString(R.string.cancel_subscription_details_title)
            subheaderRightActionTitle.text =
                getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
            subHeaderLeftIcon.setOnClickListener { finish() }
        }
        binding.cancelSubscriptionSubmit.setOnClickListener {
            cancelSubscriptionDetailsModel.onSubmitCancellation()
        }
    }

    private fun initTextWatchers() {
        val hintDate =
            DateFormat.getDateInstance(DateFormat.LONG).format(Calendar.getInstance().time)
        binding.cancellationDateSelection.hint = hintDate
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
        binding.cancellationSpecifyReasonOptionalLabel.visibility = state
        binding.cancellationSpecifyReasonLabel.visibility = state
        binding.cancellationSpecifyReasonInput.visibility = state
    }

    private fun updateCancellationDate(date: Date) {
        val validityDate = DateFormat.getDateInstance(DateFormat.LONG).format(date)
        binding.cancellationDateSelection.text = validityDate
        binding.cancellationDateLabel.text =
            getText(R.string.cancel_subscription_details_cancellation_date)
        binding.cancellationDateLabel.setTextColor(getColor(R.color.font_color_medium_grey))
    }

    private fun initSpinner() {
        val cancellationList = resources.getStringArray(R.array.cancellation_reason_list)
        val adapter = CancellationReasonAdapter(this, cancellationList)

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
                    cancelSubscriptionDetailsModel.onCancellationReason(cancellationList[position])
                }
            }
    }

    private fun displayDatePicker() {
        binding.cancelSubscriptionDetailsError.visibility = View.GONE
        val hintDate =
            DateFormat.getDateInstance(DateFormat.LONG).format(Calendar.getInstance().time)
        binding.cancellationDateSelection.hint = hintDate
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                binding.cancelSubscriptionDetailsError.visibility = View.GONE
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
        binding.cancellationDateLabel.text =
            getText(R.string.cancel_subscription_details_cancellation_date_error)
        binding.cancellationDateLabel.setTextColor(getColor(R.color.offline_red))
    }

    private fun observeViews() {
        cancelSubscriptionDetailsModel.apply {
            successDeactivation.observe {
                dialog?.cancel()
                setResult(REQUEST_TO_ACCOUNT)
                finish()
            }
        }
    }

    private fun showCancellationDialog(date: Date) {
        val formattedDate =
            DateFormat.getDateInstance(DateFormat.LONG).format(date)
        val dialogbinding = DialogCancelSubscriptionDetailsBinding.inflate(layoutInflater)
        dialog = Dialog(this, R.style.mycustomDialog)
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(dialogbinding.root)
        }
        dialogbinding.cancelSubscriptionDialogDetails.text =
            getString(R.string.cancel_subscription_dialog_content, formattedDate)
        dialogbinding.cancellationDetailDialogKeepService.setOnClickListener {
            dialog?.dismiss()
            setResult(REQUEST_TO_ACCOUNT)
            finish()
        }
        dialogbinding.cancellationDetailDialogCancelService.setOnClickListener {
            cancelSubscriptionDetailsModel.performCancellationRequest()
        }
        dialog?.show()
    }

    companion object {
        const val REQUEST_TO_CANCEL_SUBSCRIPTION: Int = 44011
        const val REQUEST_TO_ACCOUNT: Int = 43611
        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionDetailsActivity::class.java)
        }
    }
}