package com.centurylink.biwf.screens.home.dashboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.DashboardCoordinator
import com.centurylink.biwf.databinding.FragmentDashboardBinding
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.*
import kotlinx.android.synthetic.main.widget_appointment_scheduled.view.appointment_time
import kotlinx.android.synthetic.main.widget_status_enroute.view.*
import kotlinx.android.synthetic.main.widget_status_enroute.view.technician_name
import kotlinx.android.synthetic.main.widget_status_work_begun.view.*
import kotlinx.android.synthetic.main.widget_welcome_card.view.*
import javax.inject.Inject

class DashboardFragment : BaseFragment() {

    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var dashboardCoordinator: DashboardCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory
    private val dashboardViewModel by lazy {
        ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var getStartedClickListener: GetStartedEventClickListener

    private var unreadNotificationList: MutableList<Notification> = mutableListOf()
    private var enrouteMapFragment: SupportMapFragment? = null
    private var workBegunMapFragment: SupportMapFragment? = null

    //TODO: Passing hardcoded Lat long for UI testing purpose
    private var originLatLng = LatLng(39.902448, -104.97592) //LatLng(0.0, 0.0)
    private var destinationLatLng = LatLng(39.902448, -104.97592) //LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        initOnClicks()
        binding.executePendingBindings()
        dashboardViewModel.myState.observeWith(dashboardCoordinator)
        return binding.root
    }

    private fun initViews() {
        if (dashboardViewModel.isExistingUser.value) {
            incSpeedTest.visibility = View.VISIBLE
            observeNotificationViews()
        }else{
            getAppointmentStatus()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupMap()
    }

    private fun setupMap() {
        val fm = childFragmentManager

        enrouteMapFragment = fm.findFragmentById(R.id.map_enroute_status) as SupportMapFragment
        workBegunMapFragment = fm.findFragmentById(R.id.map_work_begun) as SupportMapFragment

        enrouteMapFragment?.getMapAsync(enrouteOnMapReadyCallback)
        workBegunMapFragment?.getMapAsync(mOnMapReadyCallback)
    }

    private var enrouteOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 16.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )
                addMarker(
                    MarkerOptions().position(destinationLatLng)
                        .icon(bitMapFromVector(R.drawable.blue_marker))
                )
                animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 10f))
            }
        }

    private var mOnMapReadyCallback: OnMapReadyCallback =
        OnMapReadyCallback { googleMap ->
            googleMap ?: return@OnMapReadyCallback
            with(googleMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 18.0f))
                addMarker(
                    MarkerOptions().position(originLatLng)
                        .icon(bitMapFromVector(R.drawable.green_marker))
                )
            }
        }

    private fun getAppointmentStatus() {
        dashboardViewModel.dashBoardDetailsInfo.observe {
            if (it is DashboardViewModel.AppointmentScheduleState) {
                incScheduled.visibility = View.VISIBLE
                incScheduled.appointment_date_time_card.appointment_date.text =
                    it.serviceAppointmentDate
                incScheduled.appointment_date_time_card.appointment_time.text = getString(
                    R.string.text_time_details,
                    it.serviceAppointmentStartTime,
                    it.serviceAppointmentEndTime
                )
                incScheduled.incWelcomeCard.msg_dismiss_button.setOnClickListener {
                    incScheduled.incWelcomeCard.visibility = View.GONE
                }
            }
            if (it is DashboardViewModel.AppointmentEngineerStatus) {
                incEnroute.visibility = View.VISIBLE
                incEnroute.technician_name.text = it.serviceEngineerName
                incEnroute.appointment_time.text = it.serviceAppointmentTime
                incEnroute.incEnrouteCard.title.text =
                    resources.getString(R.string.technician_on_the_way)
                incEnroute.incEnrouteCard.msg.text =
                    resources.getString(R.string.enroute_notification_message)
                incEnroute.incEnrouteCard.msg_dismiss_button.setOnClickListener {
                    incEnroute.incEnrouteCard.visibility = View.GONE
                }
                originLatLng = LatLng(it.serviceLatitude.toDouble(), it.serviceLongitude.toDouble())
                //Currently not getting technician location details, so passing hardcoded Lat long for UI testing purpose
                destinationLatLng = LatLng(39.902448, -104.97592)
            }
            if (it is DashboardViewModel.AppointmentEngineerWIP) {
                incWorkBegun.visibility = View.VISIBLE
                incWorkBegun.technician_name.text = it.serviceEngineerName
                incWorkBegun.incWipCard.title.text = resources.getString(R.string.work_in_progress)
                incWorkBegun.incWipCard.msg.text = resources.getString(R.string.work_begun_message)
                incWorkBegun.incWipCard.msg_dismiss_button.setOnClickListener {
                    incWorkBegun.incWipCard.visibility = View.GONE
                }
                originLatLng = LatLng(it.serviceLatitude.toDouble(), it.serviceLongitude.toDouble())
            }
            if (it is DashboardViewModel.AppointmentComplete) {
                incCompleted.visibility = View.VISIBLE
            }
        }
    }

    private fun observeNotificationViews() {
        dashboardViewModel.notificationListDetails.observe {
            dashboardViewModel.displaySortedNotifications(it.notificationlist)
            displaySortedNotification()
        }
    }

    private fun displaySortedNotification() {
        dashboardViewModel.notifications.observe {
            addNotificationStack(it)
        }
    }

    private fun addNotificationStack(notificationList: MutableList<Notification>) {
        unreadNotificationList = notificationList
        if (unreadNotificationList.isNotEmpty()) {
            binding.topCard.visibility = View.VISIBLE
            binding.middleCard.visibility = View.VISIBLE
            binding.bottomCard.visibility = View.VISIBLE
            when (unreadNotificationList.size) {
                1 -> {
                    binding.middleCard.visibility = View.GONE
                    binding.bottomCard.visibility = View.GONE
                }
                2 -> {
                    binding.bottomCard.visibility = View.GONE
                }
            }
            binding.notificationTitle.text = unreadNotificationList[0].name
            binding.notificationMsg.text = unreadNotificationList[0].description
        }
    }

    private fun initOnClicks() {
        binding.incScheduled.appointmentChangeBtn.setOnClickListener { dashboardViewModel.getChangeAppointment() }
        binding.notificationDismissButton.setOnClickListener {
            dashboardViewModel.markNotificationAsRead(unreadNotificationList[0])
            displaySortedNotification()
        }
        binding.topCard.setOnClickListener {
            dashboardViewModel.navigateToNotificationDetails(unreadNotificationList[0])
        }
        binding.incCompleted.getStartedBtn.setOnClickListener {
            dashboardViewModel.getStartedClicked()
            getStartedClickListener.onGetStartedClick(false)
            incCompleted.visibility = View.GONE
        }
        //TODO: Adding for testing purpose, will remove later
        binding.incScheduled.appointmentStatusTitle.setOnClickListener {
            dashboardViewModel.navigateTOEnroute()
            //incScheduled.visibility = View.GONE
            //incEnroute.visibility = View.VISIBLE

        }
        binding.incEnroute.appointmentStatusTitle.setOnClickListener {
            dashboardViewModel.navogateToWIP()
           // incScheduled.visibility = View.GONE
            //incEnroute.visibility = View.GONE
            //incWorkBegun.visibility = View.VISIBLE
        }
        binding.incWorkBegun.appointmentStatusTitle.setOnClickListener {
            dashboardViewModel.navigateToComplete()
            //incScheduled.visibility = View.GONE
            //incEnroute.visibility = View.GONE
            //incWorkBegun.visibility = View.GONE
            //incCompleted.visibility = View.VISIBLE
        }

    }

    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorResID)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun setListener(getStartedClickListener: GetStartedEventClickListener) {
        this.getStartedClickListener = getStartedClickListener
    }

    companion object {
        const val KEY_UNREAD_HEADER: String = "UNREAD_HEADER"
        private const val KEY_NEW_USER = "NEW_USER"

        operator fun invoke(newUser: Boolean) = DashboardFragment().apply {
            arguments = Bundle().apply { putBoolean(KEY_NEW_USER, newUser) }
        }
    }

    interface GetStartedEventClickListener {
        /**
         * Handle click event
         */
        fun onGetStartedClick(newUser: Boolean)
    }
}