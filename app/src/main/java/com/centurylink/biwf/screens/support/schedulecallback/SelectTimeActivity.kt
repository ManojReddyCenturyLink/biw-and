package com.centurylink.biwf.screens.support.schedulecallback

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivitySelectTimeBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.inject.Inject

class SelectTimeActivity: BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(SelectTimeViewModel::class.java)
    }
    private lateinit var binding: ActivitySelectTimeBinding
    private lateinit var timePicker: TimePickerDialog
    private var isNextAvailableSlot: Boolean = false

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
        initTextWatchers()
        initOnClicks()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        val screenTitle: String = getString(R.string.select_time)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun initTextWatchers() {
        val defaultTimeSlot = viewModel.getDefaultTimeSlot()
        binding.callbackTimeSelection.text = defaultTimeSlot
        val defaultDateSlot = viewModel.getDefaultDateSlot()
        binding.callbackDateSelection.text = defaultDateSlot
    }

    private fun initOnClicks() {
        binding.callbackDateSelection.setOnClickListener { viewModel.onDateChange() }
        binding.callbackTimeSelection.setOnClickListener { viewModel.onTimeChange() }
        binding.nextAvailableCallbackTimeRadiobtn.setOnClickListener { isNextAvailableSlot = true }
        binding.specificCallbackTimeRadiobtn.setOnClickListener { isNextAvailableSlot = false }
    }

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

    private fun setTimePickerLimits(
        selectedDate: Int, currentDate: Int, selectedMonth: Int,
        currentMonth: Int, selectedMin: Int
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

    @SuppressLint("SimpleDateFormat")
    private fun updateCallbackDate(date: Date) {
        val dateFormat = SimpleDateFormat("MM/dd/YY")
        val stringDate = dateFormat.format(date)
        binding.callbackDateSelection.text = stringDate
    }

    private fun updateCallbackTime(selectedtime: String) {
        binding.callbackTimeSelection.text = selectedtime
    }

    companion object {
        const val SELECT_TIME: String = "SelectTime"
        const val CALENDER_MAX_LIMIT: Long = 7776000000
        const val TIME_PICKER_HOUR_INTERVAL = 1
        const val TIME_PICKER_MIN_INTERVAL = 15
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, SelectTimeActivity::class.java)
                .putExtra(SELECT_TIME, bundle.getString(SELECT_TIME))
        }
    }
}