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
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.DateUtils
import javax.inject.Inject

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBookedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appointmentDate =
            intent.getStringExtra(APPOINTMENT_STATEMENT_DATE)
        appointmentSlots =
            intent.getStringExtra(APPOINTMENT_STATEMENT_SLOTS)
        appointmentId = intent.getStringExtra(APPOINTMENT_STATEMENT_ID)
        initViews()
    }

    private fun initViews() {
        val screenTitle: String = getString(R.string.booked_appointment)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.isAllCaps = true
            subheaderRightActionTitle.setOnClickListener {
                setResult(DashboardFragment.REFRESH_APPOINTMENT)
                finish()
            }

        }
        binding.viewDashboardBtn.setOnClickListener {
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

    override fun onBackPressed() {
        setResult(DashboardFragment.REFRESH_APPOINTMENT)
        finish()
    }

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