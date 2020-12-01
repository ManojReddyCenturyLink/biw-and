package com.centurylink.biwf.screens.changeappointment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityAppointmentBookedBinding
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.DateUtils
import kotlinx.android.synthetic.main.activity_appointment_booked.*
import javax.inject.Inject

/**
 * Appointment booked activity - this class handle common methods related to appointment booked screen
 *
 * @constructor Create empty Appointment booked activity
 */
class AppointmentBookedActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private lateinit var binding: ActivityAppointmentBookedBinding

    private lateinit var appointmentDate: String
    private lateinit var appointmentSlots: String
    private lateinit var appointmentId: String

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(AppointmentBookedViewModel::class.java)
    }

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBookedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appointmentDate =
            intent.getStringExtra(APPOINTMENT_STATEMENT_DATE)!!
        appointmentSlots =
            intent.getStringExtra(APPOINTMENT_STATEMENT_SLOTS)!!
        appointmentId = intent.getStringExtra(APPOINTMENT_STATEMENT_ID)!!
        initViews()
    }

    /**
     * Init views - it will initialises the views
     *
     */
    private fun initViews() {
        val screenTitle: String = getString(R.string.booked_appointment)
        if (viewModel.readAppointmentType().contains(HomeViewModel.intsall)) {
            appointment_confirmed_message.text = getString(R.string.appointment_confirmed_message)
        } else {
            appointment_confirmed_message.text = getString(R.string.service_appointment_confirmed_message)
        }
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.isAllCaps = true
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logDoneButtonClick()
                setResult(DashboardFragment.REFRESH_APPOINTMENT)
                finish()
            }
        }
        binding.viewDashboardBtn.setOnClickListener {
            viewModel.logViewDashboardButtonClick()
            setResult(DashboardFragment.REFRESH_APPOINTMENT)
            finish()
        }
        val separatedSlots = appointmentSlots.split("-".toRegex()).map { it.trim() }
        binding.appointmentConfirmedHeader.text = getString(
            R.string.appointment_confirmed_header,
            DateUtils.formatAppointmentBookedDate(appointmentDate),
            separatedSlots[0].replace("\\s".toRegex(), ""),
            separatedSlots[1].replace("\\s".toRegex(), "")
        )
    }

    /**
     * On back pressed - this handles back key click listeners
     *
     */
    override fun onBackPressed() {
        setResult(DashboardFragment.REFRESH_APPOINTMENT)
        finish()
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val APPOINTMENT_STATEMENT_SLOTS: String = "APPOINTMENT_SLOTS"
        const val APPOINTMENT_STATEMENT_DATE: String = "APPOINTMENT_STATEMENT_DATE"
        const val APPOINTMENT_STATEMENT_ID: String = "APPOINTMENT_STATEMENT_ID"
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, AppointmentBookedActivity::class.java).putExtra(
                APPOINTMENT_STATEMENT_SLOTS,
                bundle.getString(APPOINTMENT_STATEMENT_SLOTS)
            ).putExtra(APPOINTMENT_STATEMENT_DATE, bundle.getString(APPOINTMENT_STATEMENT_DATE))
                .putExtra(APPOINTMENT_STATEMENT_ID, bundle.getString(APPOINTMENT_STATEMENT_ID))
        }
    }
}
