package com.centurylink.biwf.widgets

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.Time
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import com.antonyt.infiniteviewpager.InfiniteViewPager
import com.centurylink.biwf.R
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidGridAdapter
import com.roomorama.caldroid.CaldroidListener
import com.roomorama.caldroid.CalendarHelper
import com.roomorama.caldroid.DateGridFragment
import com.roomorama.caldroid.MonthPagerAdapter
import com.roomorama.caldroid.WeekdayArrayAdapter
import hirondelle.date4j.DateTime
import java.text.ParseException
import java.util.*

/**
 * Calendar fragment -fragment class related to calender  view details
 *
 * @constructor Create empty Calendar fragment
 */
class CalendarFragment : CaldroidFragment() {

    private val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE
            or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)
    private val firstMonthTime = Time()
    private val monthYearStringBuilder = StringBuilder(50)
    private val monthYearFormatter = Formatter(
        monthYearStringBuilder, Locale.getDefault()
    )
    val NUMBER_OF_PAGES = 4

    /**
     * Cal view components
     */
    private var leftArrowButton: Button? = null
    private var rightArrowButton: Button? = null
    private var monthTitleTextView: TextView? = null
    private var weekdayGridView: GridView? = null
    private var dateViewPager: InfiniteViewPager? = null
    private var pageChangeListener: DatePageChangeListener? = null
    private var fragments: ArrayList<DateGridFragment>? = null

    private var themeResource = R.style.CaldroidDefault
    val DIALOG_TITLE = "dialogTitle"
    val MONTH = "month"
    val YEAR = "year"
    val SHOW_NAVIGATION_ARROWS = "showNavigationArrows"
    val DISABLE_DATES = "disableDates"
    val SELECTED_DATES = "selectedDates"
    val MIN_DATE = "minDate"
    val MAX_DATE = "maxDate"
    val ENABLE_SWIPE = "enableSwipe"
    val START_DAY_OF_WEEK = "startDayOfWeek"
    val SIX_WEEKS_IN_CALENDAR = "sixWeeksInCalendar"
    val ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates"
    val SQUARE_TEXT_VIEW_CELL = "squareTextViewCell"
    val THEME_RESOURCE = "themeResource"
    val _MIN_DATE_TIME = "_minDateTime"
    val _MAX_DATE_TIME = "_maxDateTime"
    val _BACKGROUND_FOR_DATETIME_MAP = "_backgroundForDateTimeMap"
    val _TEXT_COLOR_FOR_DATETIME_MAP = "_textColorForDateTimeMap"

    /**
     * A calendar height is not fixed, it may have 5 or 6 rows. Set fitAllMonths
     * to true so that the calendar will always have 6 rows
     */
    private var sixWeeksInCalendar = false

    /**
     * dateItemClickListener is fired when user click on the date cell
     */
    private var dateItemClickListener: OnItemClickListener? = null

    /**
     * dateItemLongClickListener is fired when user does a longclick on the date
     * cell
     */
    private var dateItemLongClickListener: OnItemLongClickListener? = null

    /**
     * caldroidListener inform library client of the event happens inside
     */
    private var caldroidListener: CaldroidListener? = null

    /**
     * Retrieve current month
     * @return returns month as integer value
     */
    override fun getMonth(): Int {
        return month
    }

    /**
     * Retrieve current year
     * @return returns year as integer value
     */
    override fun getYear(): Int {
        return year
    }

    /**
     * Get caldroid listener
     *
     * @return it will return the calender listener instance
     */
    override fun getCaldroidListener(): CaldroidListener? {
        return caldroidListener
    }

    /**
     * Get new dates grid adapter - initialisation of new dates grid adapter
     *
     * @param month- selected month value
     * @param year - selected year value
     * @return - returns the Caldroid Grid Adapter instance
     */
    override fun getNewDatesGridAdapter(month: Int, year: Int): CaldroidGridAdapter {
        return CaldroidGridAdapter(
            activity, month, year,
            getCaldroidData(), extraData
        )
    }

    /**
     * Get new weekday adapter - initialisation of new dates new weekday adapte
     *
     * @param themeResource  - resource value for theme
     *
     * @return returns the Weekday Array Adapter instance
     */
    override fun getNewWeekdayAdapter(themeResource: Int): WeekdayArrayAdapter {
        return WeekdayArrayAdapter(
            activity, R.layout.date_cell_view,
            daysOfWeek, themeResource
        )
    }

    /**
     * For client to customize the weekDayGridView
     *
     * @return GridView instance
     */
    override fun getWeekdayGridView(): GridView? {
        return weekdayGridView
    }

    /**
     * Get fragments
     *
     * @return- returns the array of date grid fragments
     */
    override fun getFragments(): ArrayList<DateGridFragment>? {
        return fragments
    }

    /**
     * For client wants to access dateViewPager
     *
     * @return It returns the infinite viewpager
     */
    override fun getDateViewPager(): InfiniteViewPager? {
        return dateViewPager
    }

    /**
     * For client to access background and text color maps
     *
     * @return It returns the background for date time map
     */
    override fun getBackgroundForDateTimeMap(): Map<DateTime?, Drawable?>? {
        return backgroundForDateTimeMap
    }

    /**
     * Get text color for date time map
     *
     * @return It returns the color for date time map
     */
    override fun getTextColorForDateTimeMap(): Map<DateTime?, Int?>? {
        return textColorForDateTimeMap
    }


    /**
     * Get left arrow button -  To let user customize the navigation buttons
     *
     * @return - it returns left arrow instance
     */
    override fun getLeftArrowButton(): Button? {
        return leftArrowButton
    }

    /**
     * Get right arrow button -  To let user customize the navigation buttons
     *
     * @return - it returns right arrow instance
     */
    override fun getRightArrowButton(): Button? {
        return rightArrowButton
    }

    /**
     * Get month title text view - To let client customize month title textview
     *
     * @return it returns the month title textview
     */
    override fun getMonthTitleTextView(): TextView? {
        return monthTitleTextView
    }

    /**
     * Set month title text view
     *
     * @param monthTitleTextView
     */
    override fun setMonthTitleTextView(monthTitleTextView: TextView?) {
        this.monthTitleTextView = monthTitleTextView
    }

    /**
     * Get 4 adapters of the date grid views. Useful to set custom data and
     * refresh date grid view
     *
     * @return it returns the Date PagerAdapters instance
     */
    override fun getDatePagerAdapters(): ArrayList<CaldroidGridAdapter?>? {
        return datePagerAdapters
    }

    /**
     * caldroidData return data belong to Caldroid
     *
     * @return it returns the calender data in map object
     */
    override fun getCaldroidData(): Map<String?, Any?>? {
        caldroidData.clear()
        caldroidData.put(DISABLE_DATES, disableDates)
        caldroidData.put(SELECTED_DATES, super.selectedDates)
        caldroidData.put(_MIN_DATE_TIME, super.minDateTime)
        caldroidData.put(_MAX_DATE_TIME, super.maxDateTime)
        caldroidData.put(START_DAY_OF_WEEK, super.startDayOfWeek)
        caldroidData.put(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar)
        caldroidData.put(SQUARE_TEXT_VIEW_CELL, super.squareTextViewCell)
        caldroidData.put(THEME_RESOURCE, themeResource)

        // For internal use
        caldroidData
            .put(_BACKGROUND_FOR_DATETIME_MAP, backgroundForDateTimeMap)
        caldroidData.put(_TEXT_COLOR_FOR_DATETIME_MAP, textColorForDateTimeMap)
        return caldroidData
    }

    /**
     * Set background drawable for date times
     *
     * @param backgroundForDateTimeMap - map object contains date time instance and drawable color
     */
    override fun setBackgroundDrawableForDateTimes(
        backgroundForDateTimeMap: Map<DateTime?, Drawable?>?
    ) {
        this.backgroundForDateTimeMap.putAll(backgroundForDateTimeMap!!)
    }

    /**
     * Clear background drawable for date times
     *
     * @param dateTimes -selected date times list  to clear the background
     */
    override fun clearBackgroundDrawableForDateTimes(dateTimes: List<DateTime?>?) {
        if (dateTimes == null || dateTimes.size == 0) return
        for (dateTime in dateTimes) {
            backgroundForDateTimeMap.remove(dateTime)
        }
    }

    /**
     * Set background drawable for date
     *
     * @param drawable -  drawable to set the background
     * @param date - date to set the background
     */
    override fun setBackgroundDrawableForDate(
        drawable: Drawable?,
        date: Date?
    ) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        backgroundForDateTimeMap.put(dateTime, drawable)
    }

    /**
     * Clear background drawable for date
     *
     * @param date - selected date to clear the background
     */
    override fun clearBackgroundDrawableForDate(date: Date?) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        backgroundForDateTimeMap.remove(dateTime)
    }

    /**
     * Set background drawable for date time
     *
     * @param drawable - drawable to set the background
     * @param dateTime - dateTime to set the background
     */
    override fun setBackgroundDrawableForDateTime(
        drawable: Drawable?,
        dateTime: DateTime?
    ) {
        backgroundForDateTimeMap.put(dateTime, drawable)
    }

    /**
     * Clear background drawable for date time
     *
     * @param dateTime - dateTime instance to clear the background
     */
    override fun clearBackgroundDrawableForDateTime(dateTime: DateTime?) {
        backgroundForDateTimeMap.remove(dateTime)
    }

    /**
     * Set text color for dates
     *
     * @param textColorForDateMap - text color map date instance to set the color
     */
    override fun setTextColorForDates(textColorForDateMap: Map<Date?, Int?>?) {
        if (textColorForDateMap == null || textColorForDateMap.size == 0) {
            return
        }
        textColorForDateTimeMap.clear()
        for (date in textColorForDateMap.keys) {
            val resource = textColorForDateMap[date]
            val dateTime = CalendarHelper.convertDateToDateTime(date)
            textColorForDateTimeMap.put(dateTime, resource)
        }
    }

    /**
     * Clear text color for dates
     *
     * @param dates - dates instance to clear the text color
     */
    override fun clearTextColorForDates(dates: List<Date?>?) {
        if (dates == null || dates.size == 0) return
        for (date in dates) {
            clearTextColorForDate(date)
        }
    }

    /**
     * Set text color for date times
     *
     * @param textColorForDateTimeMap- instance of dates to set text color
     */
    override fun setTextColorForDateTimes(
        textColorForDateTimeMap: Map<DateTime?, Int?>?
    ) {
        if (textColorForDateTimeMap != null) {
            this.textColorForDateTimeMap.putAll(textColorForDateTimeMap)
        }
    }

    /**
     * Set text color for date
     *
     * @param textColorRes
     * @param date
     */
    override fun setTextColorForDate(textColorRes: Int, date: Date?) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        textColorForDateTimeMap.put(dateTime, textColorRes)
    }

    /**
     * Clear text color for date
     *
     * @param date
     */
    override fun clearTextColorForDate(date: Date?) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        textColorForDateTimeMap.remove(dateTime)
    }

    /**
     * Set text color for date time
     *
     * @param textColorRes
     * @param dateTime
     */
    override fun setTextColorForDateTime(
        textColorRes: Int,
        dateTime: DateTime?
    ) {
        textColorForDateTimeMap.put(dateTime, textColorRes)
    }

    /**
     * Get current saved sates of the Caldroid. Useful for handling rotation.
     * It does not need to save state of SQUARE_TEXT_VIEW_CELL because this
     * may change on orientation change
     */
    override fun getSavedStates(): Bundle? {
        val bundle = Bundle()
        bundle.putInt(MONTH, month)
        bundle.putInt(YEAR, year)
        if (super.dialogTitle != null) {
            bundle.putString(DIALOG_TITLE, super.dialogTitle)
        }
        if (super.selectedDates != null && super.selectedDates.size > 0) {
            bundle.putStringArrayList(
                SELECTED_DATES,
                CalendarHelper.convertToStringList(super.selectedDates)
            )
        }
        if (disableDates != null && disableDates.size > 0) {
            bundle.putStringArrayList(
                DISABLE_DATES,
                CalendarHelper.convertToStringList(disableDates)
            )
        }
        if (super.minDateTime != null) {
            bundle.putString(MIN_DATE, super.minDateTime.format("YYYY-MM-DD"))
        }
        if (super.maxDateTime != null) {
            bundle.putString(MAX_DATE, super.maxDateTime.format("YYYY-MM-DD"))
        }
        bundle.putBoolean(SHOW_NAVIGATION_ARROWS, showNavigationArrows)
        bundle.putBoolean(ENABLE_SWIPE, enableSwipe)
        bundle.putInt(START_DAY_OF_WEEK, super.startDayOfWeek)
        bundle.putBoolean(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar)
        bundle.putInt(THEME_RESOURCE, themeResource)
        val args = arguments
        if (args != null && args.containsKey(SQUARE_TEXT_VIEW_CELL)) {
            bundle.putBoolean(SQUARE_TEXT_VIEW_CELL, args.getBoolean(SQUARE_TEXT_VIEW_CELL))
        }
        return bundle
    }

    /**
     * Save current state to bundle outState
     *
     * @param outState -bundle it contains the key to save
     * @param key - key to save
     */
    override fun saveStatesToKey(outState: Bundle, key: String?) {
        outState.putBundle(key, savedStates)
    }

    /**
     * Restore current states from savedInstanceState
     *
     * @param savedInstanceState - bundle it contains the key to save
     * @param key - key to save
     */
    override fun restoreStatesFromKey(savedInstanceState: Bundle?, key: String?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(key)) {
            val caldroidSavedState = savedInstanceState.getBundle(key)
            arguments = caldroidSavedState
        }
    }

    /**
     * Restore state for dialog
     *
     * @param savedInstanceState - bundle it contains the key to save
     * @param key - key to save
     * @param dialogTag - dialog identification
     */
    override fun restoreDialogStatesFromKey(
        manager: FragmentManager,
        savedInstanceState: Bundle?, key: String?, dialogTag: String?
    ) {
        restoreStatesFromKey(savedInstanceState, key)
        val existingDialog = manager
            .findFragmentByTag(dialogTag) as CaldroidFragment?
        if (existingDialog != null) {
            existingDialog.dismiss()
            show(manager, dialogTag)
        }
    }

    /**
     * Get current virtual position
     *
     * @return - returns the current page position
     */
    override fun getCurrentVirtualPosition(): Int {
        val currentPage = dateViewPager!!.currentItem
        return pageChangeListener!!.getCurrent(currentPage)
    }

    /**
     * Move calendar to the specified date
     *
     * @param date - selected to date instance
     */
    override fun moveToDate(date: Date?) {
        moveToDateTime(CalendarHelper.convertDateToDateTime(date))
    }

    /**
     * Move calendar to specified dateTime, with animation
     *
     * @param dateTime - selected to date time instance
     */
    override fun moveToDateTime(dateTime: DateTime) {
        val firstOfMonth =
            DateTime(year, month, 1, 0, 0, 0, 0)
        val lastOfMonth = firstOfMonth.endOfMonth

        // To create a swipe effect
        // Do nothing if the dateTime is in current month

        // Calendar swipe left when dateTime is in the past
        if (dateTime.lt(firstOfMonth)) {
            // Get next month of dateTime. When swipe left, month will
            // decrease
            val firstDayNextMonth = dateTime.plus(
                0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay
            )

            // Refresh adapters
            pageChangeListener!!.setCurrentDateTime(firstDayNextMonth)
            val currentItem = dateViewPager!!.currentItem
            pageChangeListener!!.refreshAdapters(currentItem)

            // Swipe left
            dateViewPager!!.currentItem = currentItem - 1
        } else if (dateTime.gt(lastOfMonth)) {
            // Get last month of dateTime. When swipe right, the month will
            // increase
            val firstDayLastMonth = dateTime.minus(
                0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay
            )

            // Refresh adapters
            pageChangeListener!!.setCurrentDateTime(firstDayLastMonth)
            val currentItem = dateViewPager!!.currentItem
            pageChangeListener!!.refreshAdapters(currentItem)

            // Swipe right
            dateViewPager!!.currentItem = currentItem + 1
        }
    }

    /**
     * Set month and year for the calendar. This is to avoid naive
     * implementation of manipulating month and year. All dates within same
     * month/year give same result
     *
     * @param date  selected to date instance
     */
    override fun setCalendarDate(date: Date?) {
        setCalendarDateTime(CalendarHelper.convertDateToDateTime(date))
    }

    /**
     * Set calendar date time
     *
     * @param dateTime - selected to date time instance
     */
    override fun setCalendarDateTime(dateTime: DateTime) {
        super.month = dateTime.month
        super.year = dateTime.year

        // Notify listener
        if (caldroidListener != null) {
            caldroidListener!!.onChangeMonth(super.month, super.year)
        }
        refreshView()
    }

    /**
     * Set calendar to previous month
     */
    override fun prevMonth() {
        dateViewPager!!.currentItem = pageChangeListener!!.currentPage - 1
    }

    /**
     * Set calendar to next month
     */
    override fun nextMonth() {
        dateViewPager!!.currentItem = pageChangeListener!!.currentPage + 1
    }

    /**
     * Clear all disable dates. Notice this does not refresh the calendar, need
     * to explicitly call refreshView()
     */
    override fun clearDisableDates() {
        disableDates.clear()
    }

    /**
     * Set disableDates from ArrayList of Date
     *
     * @param disableDateList -  selected date list to disable
     */
    override fun setDisableDates(disableDateList: ArrayList<Date?>?) {
        if (disableDateList == null || disableDateList.size == 0) {
            return
        }
        disableDates.clear()
        for (date in disableDateList) {
            val dateTime = CalendarHelper.convertDateToDateTime(date)
            disableDates.add(dateTime)
        }
    }

    /**
     * Set disableDates from ArrayList of String. By default, the date formatter
     * is yyyy-MM-dd. For e.g 2013-12-24
     *
     * @param disableDateStrings - date string instance to disable
     */
    override fun setDisableDatesFromString(disableDateStrings: ArrayList<String?>?) {
        setDisableDatesFromString(disableDateStrings, null)
    }

    /**
     * Set disableDates from ArrayList of String with custom date format. For
     * example, if the date string is 06-Jan-2013, use date format dd-MMM-yyyy.
     * This method will refresh the calendar, it's not necessary to call
     * refreshView()
     *
     * @param disableDateStrings - selected date list to disable
     * @param dateFormat - specified date format
     */
    override fun setDisableDatesFromString(
        disableDateStrings: ArrayList<String?>?,
        dateFormat: String?
    ) {
        if (disableDateStrings == null) {
            return
        }
        disableDates.clear()
        for (dateString in disableDateStrings) {
            val dateTime = CalendarHelper.getDateTimeFromString(
                dateString, dateFormat
            )
            disableDates.add(dateTime)
        }
    }

    /**
     * To clear selectedDates. This method does not refresh view, need to
     * explicitly call refreshView()
     */
    override fun clearSelectedDates() {
        selectedDates.clear()
    }

    /**
     * Select the dates from fromDate to toDate. By default the background color
     * is holo_blue_light, and the text color is black. You can customize the
     * background by changing CaldroidFragment.selectedBackgroundDrawable, and
     * change the text color CaldroidFragment.selectedTextColor before call this
     * method. This method does not refresh view, need to call refreshView()
     *
     * @param fromDate - selected from date
     * @param toDate - selected to date
     */
    override fun setSelectedDates(fromDate: Date?, toDate: Date?) {
        // Ensure fromDate is before toDate
        if (fromDate == null || toDate == null || fromDate.after(toDate)) {
            return
        }
        selectedDates.clear()
        val fromDateTime =
            CalendarHelper.convertDateToDateTime(fromDate)
        val toDateTime = CalendarHelper.convertDateToDateTime(toDate)
        var dateTime = fromDateTime
        while (dateTime.lt(toDateTime)) {
            selectedDates.add(dateTime)
            dateTime = dateTime.plusDays(1)
        }
        selectedDates.add(toDateTime)
    }

    /**
     * Convenient method to select dates from String
     *
     * @param fromDateString - selected from date
     * @param toDateString - selected to date
     * @param dateFormat - specified date format
     * @throws ParseException - exception type
     */
    @Throws(ParseException::class)
    override fun setSelectedDateStrings(
        fromDateString: String?,
        toDateString: String?, dateFormat: String?
    ) {
        val fromDate = CalendarHelper.getDateFromString(
            fromDateString,
            dateFormat
        )
        val toDate = CalendarHelper
            .getDateFromString(toDateString, dateFormat)
        setSelectedDates(fromDate, toDate)
    }

    /**
     * Set selected date
     *
     * @param date - date instance to select
     */
    override fun setSelectedDate(date: Date?) {
        if (date == null) {
            return
        }
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        super.selectedDates.add(dateTime)
    }

    /**
     * Clear selection of the specified date
     *
     * @param date -  date instance to clear selection
     */
    override fun clearSelectedDate(date: Date?) {
        if (date == null) {
            return
        }
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        super.selectedDates.remove(dateTime)
    }

    /**
     * Checks whether the specified date is selected
     *
     * @param date - date instance to check selection
     *
     * @return - it will return true if specified date is selected else it will return false
     */
    override fun isSelectedDate(date: Date?): Boolean {
        if (date == null) {
            return false
        }
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        return super.selectedDates.contains(dateTime)
    }

    /**
     * Check if the navigation arrow is shown
     *
     * @return it will return true if navigation arrows are visible else it will return false
     */
    override fun isShowNavigationArrows(): Boolean {
        return showNavigationArrows
    }

    /**
     * Show or hide the navigation arrows
     *
     * @param showNavigationArrows - boolean value to visible/invisible navigation arrows
     */
    override fun setShowNavigationArrows(showNavigationArrows: Boolean) {
        this.showNavigationArrows = showNavigationArrows
        if (showNavigationArrows) {
            leftArrowButton!!.visibility = View.VISIBLE
            rightArrowButton!!.visibility = View.VISIBLE
        } else {
            leftArrowButton!!.visibility = View.INVISIBLE
            rightArrowButton!!.visibility = View.INVISIBLE
        }
    }

    /**
     * Enable / Disable swipe to navigate different months
     *
     * @return - returns true if swipe is enabled else it will return false
     */
    override fun isEnableSwipe(): Boolean {
        return enableSwipe
    }

    /**
     * Set enable swipe
     *
     * @param enableSwipe - if value is true it will enable swipe
     */
    override fun setEnableSwipe(enableSwipe: Boolean) {
        this.enableSwipe = enableSwipe
        dateViewPager!!.isEnabled = enableSwipe
    }

    /**
     * Set min date. This method does not refresh view
     *
     * @param minDate - date instance to set as min date
     */
    override fun setMinDate(minDate: Date?) {
        if (minDate == null) {
            super.minDateTime = null
        } else {
            super.minDateTime = CalendarHelper.convertDateToDateTime(minDate)
        }
    }

    /**
     * Is six weeks in calendar
     *
     * @return - it will return true if there are 6 weeks in calender else false
     */
    override fun isSixWeeksInCalendar(): Boolean {
        return sixWeeksInCalendar
    }

    /**
     * Set six weeks in calendar
     *
     * @param sixWeeksInCalendar - if value is true then it will set 6 weeks in calender
     */
    override fun setSixWeeksInCalendar(sixWeeksInCalendar: Boolean) {
        this.sixWeeksInCalendar = sixWeeksInCalendar
        dateViewPager!!.isSixWeeksInCalendar = sixWeeksInCalendar
    }

    /**
     * Convenient method to set min date from String. If dateFormat is null,
     * default format is yyyy-MM-dd
     *
     * @param minDateString - date instance to set as min date
     * @param dateFormat - date format instance
     */
    override fun setMinDateFromString(
        minDateString: String?,
        dateFormat: String?
    ) {
        if (minDateString == null) {
            setMinDate(null)
        } else {
            minDateTime = CalendarHelper.getDateTimeFromString(
                minDateString,
                dateFormat
            )
        }
    }

    /**
     * Set max date. This method does not refresh view
     *
     * @param maxDate - max date instance
     */
    override fun setMaxDate(maxDate: Date?) {
        if (maxDate == null) {
            super.maxDateTime = null
        } else {
            super.maxDateTime = CalendarHelper.convertDateToDateTime(maxDate)
        }
    }

    /**
     * Convenient method to set max date from String. If dateFormat is null,
     * default format is yyyy-MM-dd
     *
     * @param maxDateString - date instance to set as max date
     * @param dateFormat - date format instance
     */
    override fun setMaxDateFromString(
        maxDateString: String?,
        dateFormat: String?
    ) {
        if (maxDateString == null) {
            setMaxDate(null)
        } else {
            super.maxDateTime = CalendarHelper.getDateTimeFromString(
                maxDateString,
                dateFormat
            )
        }
    }

    /**
     * Set caldroid listener when user click on a date
     *
     * @param caldroidListener - calender listner instance
     */
    override fun setCaldroidListener(caldroidListener: CaldroidListener?) {
        this.caldroidListener = caldroidListener
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return - it will return calender item click listeners
     */
    override fun getDateItemClickListener(): OnItemClickListener? {
        if (dateItemClickListener == null) {
            dateItemClickListener =
                OnItemClickListener { parent, view, position, _ ->
                    val dateTime: DateTime = super.dateInMonthsList.get(position)
                    if (caldroidListener != null) {
                        if (!enableClickOnDisabledDates) {
                            if (minDateTime != null && dateTime
                                    .lt(minDateTime)
                                || maxDateTime != null && dateTime
                                    .gt(maxDateTime)
                                || disableDates != null && disableDates
                                    .indexOf(dateTime) != -1
                            ) {
                                return@OnItemClickListener
                            }
                        }
                        val date = CalendarHelper
                            .convertDateTimeToDate(dateTime)
                        caldroidListener!!.onSelectDate(date, view)
                    }
                }
        }
        return dateItemClickListener
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return - it will return calender long item click listeners
     */
    override fun getDateItemLongClickListener(): OnItemLongClickListener? {
        if (dateItemLongClickListener == null) {
            dateItemLongClickListener =
                OnItemLongClickListener { parent, view, position, id ->
                    val dateTime: DateTime = dateInMonthsList.get(position)
                    if (caldroidListener != null) {
                        if (!enableClickOnDisabledDates) {
                            if (minDateTime != null && dateTime
                                    .lt(minDateTime)
                                || maxDateTime != null && dateTime
                                    .gt(maxDateTime)
                                || disableDates != null && disableDates
                                    .indexOf(dateTime) != -1
                            ) {
                                return@OnItemLongClickListener false
                            }
                        }
                        val date = CalendarHelper
                            .convertDateTimeToDate(dateTime)
                        caldroidListener!!.onLongClickDate(date, view)
                    }
                    true
                }
        }
        return dateItemLongClickListener
    }

    /**
     * Refresh month title text view
     *
     */
    override fun refreshMonthTitleTextView() {
        // Refresh title view
        firstMonthTime.year = year
        firstMonthTime.month = month - 1
        firstMonthTime.monthDay = 15
        val millis = firstMonthTime.toMillis(true)

        // This is the method used by the platform Calendar app to get a
        // correctly localized month name for display on a wall calendar
        monthYearStringBuilder.setLength(0)
        val monthTitle = DateUtils.formatDateRange(
            activity,
            monthYearFormatter, millis, millis, MONTH_YEAR_FLAG
        ).toString()
        monthTitleTextView!!.text = monthTitle
    }

    /**
     * Refresh view when parameter changes. You should always change all
     * parameters first, then call this method.
     */
    override fun refreshView() {
        // If month and year is not yet initialized, refreshView doesn't do
        // anything
        if (month == -1 || year == -1) {
            return
        }
        refreshMonthTitleTextView()

        // Refresh the date grid views
        for (adapter in datePagerAdapters) {
            // Reset caldroid data
            adapter.caldroidData = getCaldroidData()

            // Reset extra data
            adapter.extraData = extraData

            // Update today variable
            adapter.updateToday()

            // Refresh view
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Retrieve initial arguments to the fragment Data can include: month, year,
     * dialogTitle, showNavigationArrows,(String) disableDates, selectedDates,
     * minDate, maxDate, squareTextViewCell
     */
    override fun retrieveInitialArgs() {
        // Get arguments
        val args = arguments
        CalendarHelper.setup()
        if (args != null) {
            // Get month, year
            month = args.getInt(MONTH, -1)
            year = args.getInt(YEAR, -1)
            dialogTitle = args.getString(DIALOG_TITLE)
            val dialog = dialog
            if (dialog != null) {
                if (dialogTitle != null) {
                    dialog.setTitle(dialogTitle)
                } else {
                    // Don't display title bar if user did not supply
                    // dialogTitle
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                }
            }

            // Get start day of Week. Default calendar first column is SUNDAY
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1)
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7
            }

            // Should show arrow
            showNavigationArrows = args
                .getBoolean(SHOW_NAVIGATION_ARROWS, true)

            // Should enable swipe to change month
            enableSwipe = args.getBoolean(ENABLE_SWIPE, true)

            // Get sixWeeksInCalendar
            sixWeeksInCalendar = args.getBoolean(SIX_WEEKS_IN_CALENDAR, true)

            // Get squareTextViewCell, by default, use square cell in portrait mode
            // and using normal cell in landscape mode
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, true)
            } else {
                squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, false)
            }

            // Get clickable setting
            enableClickOnDisabledDates = args.getBoolean(
                ENABLE_CLICK_ON_DISABLED_DATES, false
            )

            // Get disable dates
            val disableDateStrings = args
                .getStringArrayList(DISABLE_DATES)
            if (disableDateStrings != null && disableDateStrings.size > 0) {
                disableDates.clear()
                for (dateString in disableDateStrings) {
                    val dt = CalendarHelper.getDateTimeFromString(
                        dateString, null
                    )
                    disableDates.add(dt)
                }
            }

            // Get selected dates
            val selectedDateStrings = args
                .getStringArrayList(SELECTED_DATES)
            if (selectedDateStrings != null && selectedDateStrings.size > 0) {
                selectedDates.clear()
                for (dateString in selectedDateStrings) {
                    val dt = CalendarHelper.getDateTimeFromString(
                        dateString, null
                    )
                    selectedDates.add(dt)
                }
            }

            // Get min date and max date
            val minDateTimeString = args.getString(MIN_DATE)
            if (minDateTimeString != null) {
                minDateTime = CalendarHelper.getDateTimeFromString(
                    minDateTimeString, null
                )
            }
            val maxDateTimeString = args.getString(MAX_DATE)
            if (maxDateTimeString != null) {
                maxDateTime = CalendarHelper.getDateTimeFromString(
                    maxDateTimeString, null
                )
            }

            // Get theme
            themeResource = args.getInt(THEME_RESOURCE, R.style.CaldroidDefault)
        }
        if (month == -1 || year == -1) {
            val dateTime =
                DateTime.today(TimeZone.getDefault())
            month = dateTime.month
            year = dateTime.year
        }
    }

    /**
     * Below code fixed the issue viewpager disappears in dialog mode on
     * orientation change
     *
     *
     * Code taken from Andy Dennie and Zsombor Erdody-Nagy
     * http://stackoverflow.com/questions/8235080/fragments-dialogfragment
     * -and-screen-rotation
     */
    override fun onDestroyView() {
        if (dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    override fun setThemeResource(id: Int) {
        themeResource = id
    }

    override fun getThemeResource(): Int {
        return themeResource
    }

    /**
     * On create view - Called to have the fragment instantiate its user interface view
     *
     * @param inflater - LayoutInflater: The LayoutInflater object that can be used to inflate
     *                   any views in the fragment
     * @param container - ViewGroup: If non-null, this is the parent view that the fragment's
     *                    UI should be attached to. The fragment should not add the view itself,
     *                    but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed from
     *                             a previous saved state as given here.
     * @return - Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retrieveInitialArgs()

        // To support keeping instance for dialog
        if (dialog != null) {
            try {
                retainInstance = true
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
        val localInflater = getThemeInflater(activity, inflater, themeResource)

        // This is a hack to fix issue localInflater doesn't use the themeResource, make Android
        // complain about layout_width and layout_height missing. I'm unsure about its impact
        // for app that wants to change theme dynamically.
        activity!!.setTheme(themeResource)
        val view =
            localInflater.inflate(R.layout.calendar_view, container, false)

        // For the monthTitleTextView
        monthTitleTextView = view
            .findViewById<View>(R.id.calendar_month_year_textview) as TextView

        // For the left arrow button
        leftArrowButton =
            view.findViewById<View>(R.id.calendar_left_arrow) as Button
        rightArrowButton = view
            .findViewById<View>(R.id.calendar_right_arrow) as Button

        // Navigate to previous month when user click
        leftArrowButton!!.setOnClickListener { prevMonth() }

        // Navigate to next month when user click
        rightArrowButton!!.setOnClickListener { nextMonth() }

        // Show navigation arrows depend on initial arguments
        isShowNavigationArrows = showNavigationArrows

        // For the weekday gridview ("SUN, MON, TUE, WED, THU, FRI, SAT")
        weekdayGridView = view.findViewById<View>(R.id.weekday_gridview) as GridView
        val weekdaysAdapter = getNewWeekdayAdapter(themeResource)
        weekdayGridView!!.adapter = weekdaysAdapter
        setupDateGridPages(view)
        refreshView()
        return view
    }

    /**
     * Setup 4 pages contain date grid views. These pages are recycled to use
     * memory efficient
     *
     * @param view - view instance to setup grid pages
     */
    private fun setupDateGridPages(view: View) {
        // Get current date time
        val currentDateTime =
            DateTime(year, month, 1, 0, 0, 0, 0)

        // Set to pageChangeListener
        pageChangeListener = DatePageChangeListener()
        pageChangeListener!!.setCurrentDateTime(currentDateTime)

        // Setup adapters for the grid views
        // Current month
        val adapter0 = getNewDatesGridAdapter(
            currentDateTime.month, currentDateTime.year
        )

        // Setup dateInMonthsList
        dateInMonthsList = adapter0.datetimeList

        // Next month
        val nextDateTime = currentDateTime.plus(
            0, 1, 0, 0, 0, 0, 0,
            DateTime.DayOverflow.LastDay
        )
        val adapter1 = getNewDatesGridAdapter(
            nextDateTime.month, nextDateTime.year
        )

        // Next 2 month
        val next2DateTime = nextDateTime.plus(
            0, 1, 0, 0, 0, 0, 0,
            DateTime.DayOverflow.LastDay
        )
        val adapter2 = getNewDatesGridAdapter(
            next2DateTime.month, next2DateTime.year
        )

        // Previous month
        val prevDateTime = currentDateTime.minus(
            0, 1, 0, 0, 0, 0, 0,
            DateTime.DayOverflow.LastDay
        )
        val adapter3 = getNewDatesGridAdapter(
            prevDateTime.month, prevDateTime.year
        )

        // Add to the array of adapters
        datePagerAdapters.add(adapter0)
        datePagerAdapters.add(adapter1)
        datePagerAdapters.add(adapter2)
        datePagerAdapters.add(adapter3)

        // Set adapters to the pageChangeListener so it can refresh the adapter
        // when page change
        pageChangeListener!!.caldroidGridAdapters = datePagerAdapters

        // Setup InfiniteViewPager and InfinitePagerAdapter. The
        // InfinitePagerAdapter is responsible
        // for reuse the fragments
        dateViewPager = view
            .findViewById<View>(R.id.months_infinite_pager) as InfiniteViewPager

        // Set enable swipe
        dateViewPager!!.isEnabled = enableSwipe

        // Set if viewpager wrap around particular month or all months (6 rows)
        dateViewPager!!.isSixWeeksInCalendar = sixWeeksInCalendar

        // Set the numberOfDaysInMonth to dateViewPager so it can calculate the
        // height correctly
        dateViewPager!!.datesInMonth = dateInMonthsList

        // MonthPagerAdapter actually provides 4 real fragments. The
        // InfinitePagerAdapter only recycles fragment provided by this
        // MonthPagerAdapter
        val pagerAdapter = MonthPagerAdapter(
            childFragmentManager
        )

        // Provide initial data to the fragments, before they are attached to
        // view.
        fragments = pagerAdapter.fragments
        for (i in 0 until NUMBER_OF_PAGES) {
            val dateGridFragment =
                (fragments as ArrayList<DateGridFragment>?)?.get(i) as DateGridFragment
            val adapter: CaldroidGridAdapter = datePagerAdapters.get(i)
            dateGridFragment.setGridViewRes(gridViewRes)
            dateGridFragment.gridAdapter = adapter
            dateGridFragment.onItemClickListener = getDateItemClickListener()
            dateGridFragment.onItemLongClickListener = getDateItemLongClickListener()
        }

        // Setup InfinitePagerAdapter to wrap around MonthPagerAdapter
        val infinitePagerAdapter = InfinitePagerAdapter(
            pagerAdapter
        )

        // Use the infinitePagerAdapter to provide data for dateViewPager
        dateViewPager!!.adapter = infinitePagerAdapter

        // Setup pageChangeListener
        dateViewPager!!.setOnPageChangeListener(pageChangeListener)
    }

    /**
     * Get days of week
     *
     * @return - it will return the days in week
     */
    override fun getDaysOfWeek(): ArrayList<String>? {
        val list2 = ArrayList<String>()
        list2.add("S")
        list2.add("M")
        list2.add("T")
        list2.add("W")
        list2.add("Th")
        list2.add("F")
        list2.add("Sa")
        return list2
    }
}