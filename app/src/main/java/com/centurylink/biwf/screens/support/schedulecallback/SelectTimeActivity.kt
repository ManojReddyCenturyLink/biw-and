package com.centurylink.biwf.screens.support.schedulecallback

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivitySelectTimeBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.inject.Inject

/**
 * Select time activity - This class handles common methods related to Select Time Screen
 *
 * @constructor Create empty Select time activity
 */
class SelectTimeActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(SelectTimeViewModel::class.java)
    }
    private lateinit var binding: ActivitySelectTimeBinding
    private lateinit var timePicker: TimePickerDialog
    private var isNextAvailableSlot: Boolean = false
    private lateinit var customerCareOption: String
    private lateinit var additionalInfo: String
    private lateinit var phoneNumber: String
    private lateinit var asap: String
    private lateinit var fullDateAndTime: String

    /**
     * On create - Called when the activity is first created
     *
     * @param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectTimeBinding.inflate(layoutInflater)
        viewModel.apply {
            changeCallbackDateEvent.handleEvent { displayDatePicker() }
            callbackDateUpdateEvent.handleEvent { updateCallbackDate(it) }
            changeCallbackTimeEvent.handleEvent { displayTimePicker() }
            callbackTimeUpdateEvent.handleEvent { updateCallbackTime(it) }
        }
        setContentView(binding.root)
        initHeaders()
        initViews()
        initOnClicks()
        observeViews()
    }

    /**
     * On back pressed - handles back key click listener
     *
     */
    override fun onBackPressed() {
        finish()
    }

    /**
     * Init headers - initializes screen headers
     *
     */
    private fun initHeaders() {
        val screenTitle: String = getString(R.string.select_time)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    /**
     * Observe views - observes the object state from View Model and makes UI changes accordingly
     *
     */
    private fun observeViews() {
        viewModel.scheduleCallbackFlow.observe {
            binding.callMeButton.visibility = if (it) View.GONE else View.VISIBLE
            binding.callMeProgressButton.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.errorFlow.observe {
            if (it) {
                CustomDialogBlueTheme(
                    getString(R.string.error_title),
                    getString(R.string.password_reset_error_msg),
                    getString(
                        R.string.discard_changes_and_close
                    ),
                    true,
                    ::onErrorDialogCallback
                ).show(
                    supportFragmentManager,
                    callingActivity?.className
                )
            }
        }
        viewModel.isScheduleCallbackSuccessful.observe {
            if (it) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    /**
     * Init text watchers - Initialises the default date and time slots
     *
     */
    private fun initViews() {
        binding.callbackTimeSelection.text =
            viewModel.getDefaultTimeSlot(LocalTime.now().minute, LocalTime.now().hour)
        binding.callbackDateSelection.text = viewModel.getDefaultDateSlot()
    }

    /**
     * On error dialog callback - Error Dialog Callback Event
     *
     * @param buttonType defines which button is clicked
     */
    private fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                binding.callbackTimeSelection.text =
                    viewModel.getDefaultTimeSlot(LocalTime.now().minute, LocalTime.now().hour)
                binding.callbackDateSelection.text = viewModel.getDefaultDateSlot()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    /**
     * Init on clicks - Initializes the click events
     */
    private fun initOnClicks() {
        binding.callbackDateSelection.setOnClickListener { viewModel.onDateChange() }
        binding.callbackTimeSelection.setOnClickListener { viewModel.onTimeChange() }
        binding.nextAvailableCallbackTimeRadiobtn.setOnClickListener { isNextAvailableSlot = true }
        binding.specificCallbackTimeRadiobtn.setOnClickListener { isNextAvailableSlot = false }
        binding.callMeButton.setOnClickListener {
            customerCareOption = intent.getStringExtra(SELECT_TIME)
            additionalInfo = intent.getStringExtra(ADDITIONAL_INFO)
            phoneNumber = intent.getStringExtra(PHONE_NUMBER)
            if (binding.nextAvailableCallbackTimeRadiobtn.isChecked) {
                asap = "true"
                fullDateAndTime = ""
            } else {
                asap = "false"
                fullDateAndTime = viewModel.formatDateAndTime(
                    binding.callbackDateSelection.text,
                    binding.callbackTimeSelection.text
                )
            }
            viewModel.supportService(
                phoneNumber,
                asap,
                customerCareOption,
                fullDateAndTime,
                additionalInfo
            )
        }
    }

    /**
     * Display date picker - Displays the date picker dialog
     *
     */
    private fun displayDatePicker() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            this, R.style.style_callback_date_time_picker,
            DatePickerDialog.OnDateSetListener { _, yearVal, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(yearVal, monthOfYear, dayOfMonth)
                viewModel.onCallbackDateSelected(newDate.time)
            },
            year,
            month,
            day
        )
        datePicker.datePicker.maxDate = cal.timeInMillis + CALENDER_MAX_LIMIT
        datePicker.datePicker.minDate = cal.timeInMillis
        datePicker.show()
    }

    /**
     * Display time picker - displays the custom time picker dialog
     *
     */
    @SuppressLint("SimpleDateFormat")
    private fun displayTimePicker() {
        timePicker = TimePickerDialog.newInstance(
            { _, hour, min, sec ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(0, 0, 0, hour, min, sec)
                val timeSelected = SimpleDateFormat("hh:mma").format(newDate.time)
                viewModel.onCallbackTimeSelected(timeSelected)
            }, false
        )
        timePicker.vibrate(false)
        timePicker.accentColor = getColor(R.color.purple)
        timePicker.setTimeInterval(TIME_PICKER_HOUR_INTERVAL, TIME_PICKER_MIN_INTERVAL)
        val selectedDateFull = binding.callbackDateSelection.text
        val selectedMonth = Integer.parseInt(selectedDateFull.substring(0, 2))
        val selectedDate = Integer.parseInt(selectedDateFull.substring(3, 5))

        val selectedTimeFull = binding.callbackTimeSelection.text
        val selectedHour = Integer.parseInt(selectedTimeFull.substring(0, 2))
        val selectedMin = Integer.parseInt(selectedTimeFull.substring(3, 5))
        val currentDate = LocalDate.now().dayOfMonth
        val currentMonth = LocalDate.now().monthValue

        timePicker.setInitialSelection(selectedHour, selectedMin)
        setTimePickerLimits(selectedDate, currentDate, selectedMonth, currentMonth, selectedMin)
        timePicker.show(supportFragmentManager, "Time Picker")
    }

    /**
     * Set time picker limits - sets the minimum and maximum time slots that can be selected in the
     *                          time picker dialog
     *
     * @param selectedDate - date value that user has selected in the dialog
     * @param currentDate - current date at the time of selection
     * @param selectedMonth - month value that user has selected in the dialog
     * @param currentMonth - month that user has selected in the dialog
     * @param selectedMin - minute value that user has selected in the dialog
     */
    private fun setTimePickerLimits(
        selectedDate: Int,
        currentDate: Int,
        selectedMonth: Int,
        currentMonth: Int,
        selectedMin: Int
    ) {
        if (selectedDate > currentDate && selectedMonth == currentMonth) {
            timePicker.setMinTime(0, 0, 0)
        } else if (selectedDate < currentDate && selectedMonth != currentMonth) {
            timePicker.setMinTime(0, 0, 0)
        } else if (selectedDate == currentDate && selectedMonth == currentMonth) {
            val hour: Int = LocalTime.now().hour
            var ampm = 0
            if (hour in 12..23) {
                ampm = 1
            } else if (hour in 0..11) {
                ampm = 0
            }
            if (LocalTime.now().minute in 45..59) {
                if (ampm == 1) {
                    timePicker.isAmDisabled
                }
                timePicker.setMinTime(hour + 1, 0, 0)
            } else {
                timePicker.setMinTime(hour, selectedMin, 0)
            }
        }
    }

    /**
     * Update callback date - updates the selected date from date picker dialog in UI
     *
     * @param date - date value selected by user
     */
    @SuppressLint("SimpleDateFormat")
    private fun updateCallbackDate(date: Date) {
        val dateFormat = SimpleDateFormat("MM/dd/YY")
        val stringDate = dateFormat.format(date)
        binding.callbackDateSelection.text = stringDate
    }

    /**
     * Update callback time - updates the selected time from time picker dialog in UI
     *
     * @param selectedtime - time value selected by user
     */
    private fun updateCallbackTime(selectedtime: String) {
        binding.callbackTimeSelection.text = selectedtime
    }

    companion object {
        const val SELECT_TIME: String = "SelectTime"
        const val ADDITIONAL_INFO: String = "AdditionalInfo"
        const val PHONE_NUMBER: String = "PhoneNumber"
        const val CALENDER_MAX_LIMIT: Long = 7776000000
        const val TIME_PICKER_HOUR_INTERVAL = 1
        const val TIME_PICKER_MIN_INTERVAL = 15
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, SelectTimeActivity::class.java)
                .putExtra(SELECT_TIME, bundle.getString(SELECT_TIME))
                .putExtra(ADDITIONAL_INFO, bundle.getString(ADDITIONAL_INFO))
                .putExtra(PHONE_NUMBER, bundle.getString(PHONE_NUMBER))
        }
    }
}
