package com.centurylink.biwf.screens.changeappointment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.R.string.available_appointments_on_date
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.ChangeAppointmentCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityChangeAppointmentBinding
import com.centurylink.biwf.screens.changeappointment.adapter.AppointmentSlotsAdapter
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.widgets.CalendarFragment
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidListener
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChangeAppointmentActivity : BaseActivity(), AppointmentSlotsAdapter.SlotClickListener {

    @Inject
    lateinit var changeAppointmentCoordinator: ChangeAppointmentCoordinator

    private lateinit var appointmentSlotAdapter: AppointmentSlotsAdapter

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private lateinit var binding: ActivityChangeAppointmentBinding

    private var selectedDate: String = ""

    private var selectedSlot: String = ""

    private lateinit var calendarFragment: CalendarFragment

    private var finalSlotMap: Map<String, List<String>> = mapOf()

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(ChangeAppointmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.appointmentModifyView,
            binding.retryOverlay.root
        )
        navigator.observe(this)
        viewModel.apply {
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            sloterrorEvents.handleEvent { displaySlotError() }
            appointmenterrorEvents.handleEvent { displayAppointmentError() }
        }
        viewModel.myState.observeWith(changeAppointmentCoordinator)
        initViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TO_DASHBOARD -> {
                if (resultCode == DashboardFragment.REFRESH_APPOINTMENT) {
                    setResult(DashboardFragment.REFRESH_APPOINTMENT)
                    finish()
                }
            }
        }
    }

    override fun retryClicked() {
        viewModel.initApis()
    }

    private fun displaySlotError() {
        binding.incHeader.apply {
            subheaderRightActionTitle.isEnabled = true
            subheaderRightActionTitle.isClickable = true
        }
        binding.availableAppointmentSlotError.visibility = View.VISIBLE
    }

    private fun displayAppointmentError() {
        binding.incHeader.apply {
            subheaderRightActionTitle.isEnabled = true
            subheaderRightActionTitle.isClickable = true
        }
        binding.errorInSelectedSlot.visibility = View.VISIBLE
    }

    private fun initViews() {
        val screenTitle: String = getString(R.string.modify_appointments)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackClick()
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.next)
            subheaderRightActionTitle.isAllCaps = true
            subheaderRightActionTitle.isEnabled = true
            subheaderRightActionTitle.isClickable = true
            subheaderRightActionTitle.setOnClickListener {
                if (!selectedDate.isNullOrEmpty()) {
                    viewModel.onNextClicked(
                        selectedDate,
                        selectedSlot
                    )
                }
                if (!selectedSlot.isNullOrEmpty()) {
                    subheaderRightActionTitle.isEnabled = false
                    subheaderRightActionTitle.isClickable = false
                }
            }
        }
        initCalendar()
        initSlotsViews()
        observeViews()
    }

    private fun initSlotsViews() {
        binding.availableAppointmentSlotsRv.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        appointmentSlotAdapter = AppointmentSlotsAdapter(emptyList(), this)
        binding.availableAppointmentSlotsRv.adapter = appointmentSlotAdapter
    }

    private fun observeViews() {
        viewModel.appointmentSlotsInfo.observe {
            binding.availableAppointmentNote.text =
                getString(
                    available_appointments_on_date,
                    DateUtils.formatAppointmentBookedDate(it.serviceDate.toString())
                )
            val firstSlotDate = SimpleDateFormat(DateUtils.STANDARD_FORMAT).parse(it.serviceDate)
            selectedDate = it.serviceDate!!
            selectedSlot = ""
            setCustomResourceForSelectedDate(calendarFragment, firstSlotDate)
            appointmentSlotAdapter.slotList = it.availableSlotsForDate
            appointmentSlotAdapter.lastSelectedPosition = -1
            appointmentSlotAdapter.notifyDataSetChanged()
            iterateBetweenDates(
                DateUtils.getFirstDateofthisMonth(),
                DateUtils.getLastDateoftheMonthAfter(),
                it.finalSlotMap
            )
        }
    }

    private fun initCalendar() {
        calendarFragment = CalendarFragment()
        val args = Bundle()
        val cal = Calendar.getInstance()
        var previousDate = cal.time
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1)
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR))
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true)
        args.putBoolean(CaldroidFragment.MONTH, false)
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, false)
        args.putString(
            CaldroidFragment._MAX_DATE_TIME,
            DateUtils.toSimpleString(
                DateUtils.getLastDateoftheMonthAfter(),
                DateUtils.STANDARD_FORMAT
            )
        )
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidDefaultLight)
        calendarFragment.setMinDate(DateUtils.getFirstDateofthisMonth())
        calendarFragment.setMaxDate(DateUtils.getLastDateoftheMonthAfter())
        calendarFragment.arguments = args
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.calendar_view, calendarFragment)
        fragmentTransaction.commit()
        calendarFragment.caldroidListener = object : CaldroidListener() {

            override fun onChangeMonth(month: Int, year: Int) {
                super.onChangeMonth(month, year)
                val cal = Calendar.getInstance()
                val currentMonth = cal[Calendar.MONTH] + 1
                if (currentMonth == month) {
                    calendarFragment.leftArrowButton!!.visibility = View.GONE
                } else {
                    calendarFragment.leftArrowButton!!.visibility = View.VISIBLE
                }
                val updatedCal = Calendar.getInstance()
                updatedCal.add(Calendar.DATE, 60)
                updatedCal.set(
                    Calendar.DAY_OF_MONTH,
                    updatedCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                val finalMonth = updatedCal[Calendar.MONTH] + 1
                if (finalMonth == month) {
                    calendarFragment.rightArrowButton!!.visibility = View.GONE
                } else {
                    calendarFragment.rightArrowButton!!.visibility = View.VISIBLE
                }
            }

            override fun onSelectDate(date: Date?, view: View?) {
                if (finalSlotMap != null && finalSlotMap.containsKey(
                        DateUtils.toSimpleString(
                            date!!, DateUtils.STANDARD_FORMAT
                        )
                    )
                ) {
                    val oldselectedDate =
                        SimpleDateFormat(DateUtils.STANDARD_FORMAT).parse(selectedDate)
                    calendarFragment.clearBackgroundDrawableForDate(oldselectedDate)
                    calendarFragment.setTextColorForDate(R.color.purple, oldselectedDate)
                    calendarFragment.clearSelectedDates()
                    calendarFragment.setSelectedDate(date)
                    setCustomResourceForSelectedDate(calendarFragment, date)
                    previousDate = date
                    selectedSlot = ""
                    selectedDate = DateUtils.toSimpleString(date, DateUtils.STANDARD_FORMAT)
                    calendarFragment.refreshView()
                    viewModel.onAppointmentSelectedDate(date)
                }
            }
        }
    }

    /*Function to apply style to selected date*/
    private fun setCustomResourceForSelectedDate(
        calendarFragment: CalendarFragment,
        date: Date?
    ) {
        val background = getDrawable(R.drawable.blue_marker)
        calendarFragment.setBackgroundDrawableForDate(background, date)
        calendarFragment.setTextColorForDate(R.color.white, date)
    }

    /*Function to disable dates for which slots are not available*/
    private fun setDisableDates(
        calendarFragment: CalendarFragment,
        date: ArrayList<Date?>
    ) {
        date.forEach {
            calendarFragment.setTextColorForDate(R.color.grey_text_color, it)
        }
        calendarFragment.setDisableDates(date)
        calendarFragment.refreshView()
    }

    /*Function to disable month arrow for which slots are not available*/
    private fun disableNavigationArrow(
        arrowButton: Button?
    ) {
        arrowButton?.background = getDrawable(R.drawable.ic_blue_back_navigation)
        arrowButton?.isClickable = false
    }

    companion object {
        val REQUEST_TO_DASHBOARD = 1100
        fun newIntent(context: Context) = Intent(context, ChangeAppointmentActivity::class.java)
    }

    private fun iterateBetweenDates(start: Date, end: Date, sortedMap: Map<String, List<String>>) {
        var current = DateUtils.addDays(start, -10)
        var finalEndDate = DateUtils.addDays(end, 15)
        while (current!!.before(finalEndDate)) {
            updateCalendarView(sortedMap, current)
            val calendar = Calendar.getInstance()
            calendar.time = current
            calendar.add(Calendar.DATE, 1)
            current = calendar.time
        }
    }

    private fun updateCalendarView(sortedMap: Map<String, List<String>>, currentDate: Date) {
        val disableDateList = ArrayList<Date?>()
        finalSlotMap = sortedMap
        if (!sortedMap.containsKey(
                DateUtils.toSimpleString(
                    currentDate,
                    DateUtils.STANDARD_FORMAT
                )
            )
        ) {
            disableDateList.add(currentDate)
        }
        if (!disableDateList.isNullOrEmpty()) {
            setDisableDates(calendarFragment = calendarFragment, date = disableDateList)
        }
    }

    override fun onSlotSelected(slotInfo: String) {
        binding.availableAppointmentSlotError.visibility = View.GONE
        selectedSlot = slotInfo
    }
}